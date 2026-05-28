package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.enums.TaskStatus;
import com.ecommerce.detail.ai.dto.PostProcessTaskCreateDTO;
import com.ecommerce.detail.ai.dto.PostProcessTaskDTO;
import com.ecommerce.detail.ai.dto.PostProcessTaskListQuery;
import com.ecommerce.detail.ai.dto.tool.ToolAdapterInfoDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeRequestDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeResponseDTO;
import com.ecommerce.detail.ai.entity.GenerationResult;
import com.ecommerce.detail.ai.entity.PostProcessTask;
import com.ecommerce.detail.ai.exception.ResourceNotFoundException;
import com.ecommerce.detail.ai.mapper.GenerationResultMapper;
import com.ecommerce.detail.ai.mapper.PostProcessTaskMapper;
import com.ecommerce.detail.ai.service.PostProcessTaskService;
import com.ecommerce.detail.ai.service.ToolAdapterService;
import com.ecommerce.detail.ai.util.LocalPathPolicy;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PostProcessTaskServiceImpl extends ServiceImpl<PostProcessTaskMapper, PostProcessTask>
        implements PostProcessTaskService {

    private static final List<String> DEFAULT_INPUT_ROOTS = List.of("exports", "uploads");
    private static final List<String> DEFAULT_OUTPUT_ROOTS = List.of("exports/post-process");
    private static final Set<String> ALLOWED_OPERATIONS = Set.of(
            "inpaint", "cleanup-background", "remove-object",
            "segment", "upscale", "restore-face", "enhance-main-image",
            "crop", "resize", "convert", "compose", "stitch");

    @Autowired(required = false)
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired(required = false)
    private ToolAdapterService toolAdapterService;

    @Autowired(required = false)
    private GenerationResultMapper generationResultMapper;

    @Override
    public PageResult<PostProcessTaskDTO> listPostProcessTasks(PostProcessTaskListQuery query) {
        int pageNum = Math.max(query.getPageNum() != null ? query.getPageNum() : 1, 1);
        int pageSize = query.getPageSize() != null ? query.getPageSize() : 20;
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        LambdaQueryWrapper<PostProcessTask> wrapper = new LambdaQueryWrapper<>();
        if (query.getSourceGenerationResultId() != null) {
            wrapper.eq(PostProcessTask::getSourceGenerationResultId, query.getSourceGenerationResultId());
        }
        if (StringUtils.hasText(query.getToolCode())) {
            wrapper.eq(PostProcessTask::getToolCode, query.getToolCode().trim());
        }
        if (StringUtils.hasText(query.getOperation())) {
            wrapper.eq(PostProcessTask::getOperation, query.getOperation().trim());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(PostProcessTask::getStatus, TaskStatus.normalize(query.getStatus(), "PENDING"));
        }
        wrapper.orderByDesc(PostProcessTask::getCreateTime);

        Page<PostProcessTask> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<PostProcessTaskDTO> records = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.success(records, pageNum, pageSize, page.getTotal());
    }

    @Override
    public Long createPostProcessTask(PostProcessTaskCreateDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("request body must not be null");
        }

        String toolCode = requireText(dto.getToolCode(), "toolCode");
        String operation = requireText(dto.getOperation(), "operation");
        if (!ALLOWED_OPERATIONS.contains(operation)) {
            throw new IllegalArgumentException("unsupported operation: " + operation);
        }

        // Resolve source image path
        String sourceImagePath = resolveSourceImagePath(dto);
        validateLocalPath(sourceImagePath, "source image");

        PostProcessTask task = new PostProcessTask();
        task.setSourceGenerationResultId(dto.getSourceGenerationResultId());
        task.setSourceImagePath(sourceImagePath);
        task.setToolCode(toolCode);
        task.setOperation(operation);
        task.setParamsJson(writeJson(dto.getParams()));
        task.setStatus(TaskStatus.PENDING.getCode());
        task.setProgress(0);
        task.setCreateTime(now());
        task.setUpdateTime(now());

        // Read input image metadata
        readInputMetadata(task);

        // Submit to tool adapter
        submitToTool(task, dto);

        this.save(task);
        return task.getId();
    }

    @Override
    public PostProcessTaskDTO getPostProcessTaskById(Long id) {
        return toDTO(requireTask(id));
    }

    @Override
    public boolean retryPostProcessTask(Long id) {
        PostProcessTask task = requireTask(id);
        if (!TaskStatus.FAILED.getCode().equals(task.getStatus())
                && !TaskStatus.CANCELED.getCode().equals(task.getStatus())) {
            throw new IllegalStateException("only failed or canceled tasks can be retried");
        }

        task.setErrorMessage(null);
        task.setProgress(0);
        task.setOutputImagePath(null);
        task.setOutputWidth(null);
        task.setOutputHeight(null);
        task.setOutputFileSize(null);
        task.setOutputMimeType(null);
        task.setStatus(TaskStatus.PENDING.getCode());
        task.setUpdateTime(now());

        PostProcessTaskCreateDTO retryDto = new PostProcessTaskCreateDTO();
        retryDto.setSourceGenerationResultId(task.getSourceGenerationResultId());
        retryDto.setSourceImagePath(task.getSourceImagePath());
        retryDto.setToolCode(task.getToolCode());
        retryDto.setOperation(task.getOperation());
        retryDto.setParams(readMap(task.getParamsJson()));

        submitToTool(task, retryDto);
        return this.updateById(task);
    }

    @Override
    public boolean cancelPostProcessTask(Long id) {
        PostProcessTask task = requireTask(id);
        if (!TaskStatus.PENDING.getCode().equals(task.getStatus())
                && !TaskStatus.RUNNING.getCode().equals(task.getStatus())) {
            throw new IllegalStateException("only pending or running tasks can be canceled");
        }
        task.setStatus(TaskStatus.CANCELED.getCode());
        task.setErrorMessage("Post-process task canceled");
        task.setUpdateTime(now());
        return this.updateById(task);
    }

    @Override
    public boolean updateTaskStatus(Long id, String status, Integer progress, String errorMessage) {
        PostProcessTask task = requireTask(id);
        if (isTerminalStatus(task.getStatus()) && !task.getStatus().equals(status)) {
            throw new IllegalStateException("terminal post-process tasks cannot change status");
        }
        if (StringUtils.hasText(status)) {
            task.setStatus(TaskStatus.normalize(status, task.getStatus()));
        }
        if (progress != null) {
            task.setProgress(Math.max(progress, 0));
        }
        if (errorMessage != null) {
            task.setErrorMessage(errorMessage);
        }
        task.setUpdateTime(now());
        return this.updateById(task);
    }

    // -- Private helpers --

    private String resolveSourceImagePath(PostProcessTaskCreateDTO dto) {
        if (StringUtils.hasText(dto.getSourceImagePath())) {
            return dto.getSourceImagePath().trim();
        }
        if (dto.getSourceGenerationResultId() != null && generationResultMapper != null) {
            GenerationResult result = generationResultMapper.selectById(dto.getSourceGenerationResultId());
            if (result == null) {
                throw new ResourceNotFoundException("Generation result not found: " + dto.getSourceGenerationResultId());
            }
            if (StringUtils.hasText(result.getResultUrl())) {
                return result.getResultUrl();
            }
            throw new IllegalArgumentException("Generation result has no source image: " + dto.getSourceGenerationResultId());
        }
        throw new IllegalArgumentException("either sourceImagePath or sourceGenerationResultId is required");
    }

    private void validateLocalPath(String rawPath, String label) {
        if (!StringUtils.hasText(rawPath)) {
            return;
        }
        try {
            List<Path> allowedRoots = LocalPathPolicy.parseAllowedRoots(null, DEFAULT_INPUT_ROOTS);
            LocalPathPolicy.requirePathWithinRoots(rawPath, allowedRoots, label);
        } catch (IllegalArgumentException e) {
            // Allow HTTP/HTTPS URLs and non-local paths through -- only enforce local-root policy on local files
            if (rawPath.startsWith("http://") || rawPath.startsWith("https://")) {
                return;
            }
            throw e;
        }
    }

    private void readInputMetadata(PostProcessTask task) {
        if (!StringUtils.hasText(task.getSourceImagePath())) {
            return;
        }
        try {
            Path path = LocalPathPolicy.toAbsolutePath(task.getSourceImagePath(), "source image");
            if (Files.exists(path) && Files.isRegularFile(path)) {
                task.setInputFileSize(Files.size(path));
                String mimeType = Files.probeContentType(path);
                if (StringUtils.hasText(mimeType)) {
                    task.setInputMimeType(mimeType);
                }
            }
        } catch (IOException | IllegalArgumentException ignored) {
            // Non-fatal: metadata is best-effort
        }
    }

    private void submitToTool(PostProcessTask task, PostProcessTaskCreateDTO dto) {
        if (toolAdapterService == null) {
            markToolUnavailable(task, "Tool adapter service is not available");
            return;
        }

        try {
            ToolAdapterInfoDTO tool = toolAdapterService.getTool(task.getToolCode());
            if (tool == null || !tool.isConfigured()) {
                markToolUnavailable(task, "Tool adapter not configured: " + task.getToolCode());
                return;
            }

            ToolInvokeRequestDTO request = new ToolInvokeRequestDTO();
            request.setOperation(task.getOperation());

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sourceImagePath", task.getSourceImagePath());
            payload.put("operation", task.getOperation());
            if (StringUtils.hasText(task.getParamsJson())) {
                Map<String, Object> params = readMap(task.getParamsJson());
                if (params != null) {
                    payload.putAll(params);
                }
            }
            if (dto != null) {
                if (StringUtils.hasText(dto.getMaskImagePath())) {
                    payload.put("maskImagePath", dto.getMaskImagePath());
                }
                if (dto.getTargetWidth() != null) {
                    payload.put("targetWidth", dto.getTargetWidth());
                }
                if (dto.getTargetHeight() != null) {
                    payload.put("targetHeight", dto.getTargetHeight());
                }
                if (StringUtils.hasText(dto.getOutputRatio())) {
                    payload.put("outputRatio", dto.getOutputRatio());
                }
            }
            request.setPayload(payload);

            ToolInvokeResponseDTO response = toolAdapterService.invoke(task.getToolCode(), request);
            if (response != null && response.getStatusCode() != null
                    && response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                task.setStatus(TaskStatus.RUNNING.getCode());
                task.setErrorMessage(null);
            } else {
                task.setStatus(TaskStatus.FAILED.getCode());
                task.setErrorMessage("Tool adapter returned status: "
                        + (response != null ? response.getStatusCode() : "null"));
            }
            task.setProgress(0);
            task.setUpdateTime(now());
        } catch (UnsupportedOperationException | IllegalStateException e) {
            markToolUnavailable(task, e.getMessage());
        } catch (RuntimeException e) {
            task.setStatus(TaskStatus.FAILED.getCode());
            task.setErrorMessage(e.getMessage());
            task.setUpdateTime(now());
        }
    }

    private void markToolUnavailable(PostProcessTask task, String reason) {
        task.setStatus(TaskStatus.CANCELED.getCode());
        task.setProgress(0);
        task.setErrorMessage(reason);
        task.setUpdateTime(now());
    }

    private PostProcessTask requireTask(Long id) {
        PostProcessTask task = this.getById(id);
        if (task == null) {
            throw new ResourceNotFoundException("Post-process task not found: " + id);
        }
        return task;
    }

    private PostProcessTaskDTO toDTO(PostProcessTask task) {
        if (task == null) {
            return null;
        }
        PostProcessTaskDTO dto = new PostProcessTaskDTO();
        dto.setId(task.getId());
        dto.setSourceGenerationResultId(task.getSourceGenerationResultId());
        dto.setSourceImagePath(task.getSourceImagePath());
        dto.setOutputImagePath(task.getOutputImagePath());
        dto.setToolCode(task.getToolCode());
        dto.setOperation(task.getOperation());
        dto.setParams(readMap(task.getParamsJson()));
        dto.setInputWidth(task.getInputWidth());
        dto.setInputHeight(task.getInputHeight());
        dto.setInputFileSize(task.getInputFileSize());
        dto.setInputMimeType(task.getInputMimeType());
        dto.setOutputWidth(task.getOutputWidth());
        dto.setOutputHeight(task.getOutputHeight());
        dto.setOutputFileSize(task.getOutputFileSize());
        dto.setOutputMimeType(task.getOutputMimeType());
        dto.setSourceChain(readListMap(task.getSourceChainJson()));
        dto.setStatus(task.getStatus());
        dto.setProgress(task.getProgress());
        dto.setErrorMessage(task.getErrorMessage());
        dto.setCreateTime(task.getCreateTime());
        dto.setUpdateTime(task.getUpdateTime());
        return dto;
    }

    private String writeJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> readMap(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return null;
        }
    }

    private List<Map<String, Object>> readListMap(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return null;
        }
    }

    private static String requireText(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }

    private static boolean isTerminalStatus(String status) {
        return TaskStatus.SUCCEEDED.getCode().equals(status)
                || TaskStatus.FAILED.getCode().equals(status)
                || TaskStatus.CANCELED.getCode().equals(status);
    }

    private static LocalDateTime now() {
        return LocalDateTime.now();
    }
}



