package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.enums.TaskStatus;
import com.ecommerce.detail.ai.dto.ImageJobCreateDTO;
import com.ecommerce.detail.ai.dto.ImageJobDTO;
import com.ecommerce.detail.ai.dto.ImageJobRetryDTO;
import com.ecommerce.detail.ai.dto.ImageJobStatusDTO;
import com.ecommerce.detail.ai.dto.tool.ToolAdapterInfoDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeRequestDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeResponseDTO;
import com.ecommerce.detail.ai.entity.ImageJob;
import com.ecommerce.detail.ai.mapper.ImageJobMapper;
import com.ecommerce.detail.ai.service.ImageJobService;
import com.ecommerce.detail.ai.service.ToolAdapterService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ImageJobServiceImpl extends ServiceImpl<ImageJobMapper, ImageJob> implements ImageJobService {

    @Autowired(required = false)
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired(required = false)
    private ToolAdapterService toolAdapterService;

    @Override
    public PageResult<ImageJobDTO> listImageJobs(int pageNum, int pageSize, String keyword, String status, String toolCode, Long visualPlanId) {
        pageNum = Math.max(pageNum, 1);
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        LambdaQueryWrapper<ImageJob> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(q -> q.like(ImageJob::getTaskName, keyword)
                    .or()
                    .like(ImageJob::getToolCode, keyword)
                    .or()
                    .like(ImageJob::getExternalJobId, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(ImageJob::getStatus, normalizeStatus(status));
        }
        if (visualPlanId != null) {
            wrapper.eq(ImageJob::getVisualPlanId, visualPlanId);
        }
        if (StringUtils.hasText(toolCode)) {
            wrapper.eq(ImageJob::getToolCode, toolCode.trim());
        }
        wrapper.orderByDesc(ImageJob::getCreateTime);

        Page<ImageJob> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<ImageJobDTO> records = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.success(records, pageNum, pageSize, page.getTotal());
    }

    @Override
    public Long createImageJob(ImageJobCreateDTO dto) {
        ImageJob job = new ImageJob();
        job.setTaskName(requireText(dto == null ? null : dto.getTaskName(), "taskName"));
        job.setToolCode(requireText(dto == null ? null : dto.getToolCode(), "toolCode"));
        job.setInputJson(writeJson(dto == null ? null : dto.getInputData()));
        job.setVisualPlanId(dto == null ? null : dto.getVisualPlanId());
        job.setSlot(dto == null ? null : dto.getSlot());
        job.setRatio(dto == null ? null : dto.getRatio());
        job.setPromptVersion(dto == null ? null : dto.getPromptVersion());
        job.setModelProfileId(dto == null ? null : dto.getModelProfileId());
        job.setSourceSnapshotJson(dto == null ? null : dto.getSourceSnapshotJson());
        job.setStatus(TaskStatus.PENDING.getCode());
        job.setProgress(0);
        job.setCreateTime(now());
        job.setUpdateTime(now());
        submitToToolIfPossible(job, false);
        this.save(job);
        return job.getId();
    }

    @Override
    public ImageJobDTO getImageJobById(Long id) {
        return toDTO(requireJob(id));
    }

    @Override
    public boolean updateImageJobStatus(Long id, ImageJobStatusDTO dto) {
        ImageJob job = requireJob(id);
        if (dto != null && StringUtils.hasText(dto.getStatus())) {
            String nextStatus = normalizeStatus(dto.getStatus());
            if (isTerminalStatus(job.getStatus()) && !job.getStatus().equals(nextStatus)) {
                throw new IllegalStateException("terminal image jobs cannot change status");
            }
            job.setStatus(nextStatus);
        }
        if (dto != null && dto.getProgress() != null) {
            job.setProgress(Math.max(dto.getProgress(), 0));
        }
        if (dto != null && dto.getErrorMessage() != null) {
            job.setErrorMessage(dto.getErrorMessage());
        }
        job.setUpdateTime(now());
        return this.updateById(job);
    }

    @Override
    public boolean retryImageJob(Long id, ImageJobRetryDTO dto) {
        ImageJob job = requireJob(id);
        if (!TaskStatus.FAILED.getCode().equals(job.getStatus())
                && !TaskStatus.CANCELED.getCode().equals(job.getStatus())) {
            throw new IllegalStateException("only failed or canceled jobs can be retried");
        }

        if (dto != null && StringUtils.hasText(dto.getRetryReason())) {
            job.setErrorMessage(dto.getRetryReason().trim());
        } else {
            job.setErrorMessage(null);
        }
        job.setExternalJobId(null);
        job.setProgress(0);
        job.setStatus(TaskStatus.PENDING.getCode());
        job.setUpdateTime(now());

        submitToToolIfPossible(job, true);
        return this.updateById(job);
    }

    @Override
    public boolean cancelImageJob(Long id, ImageJobStatusDTO dto) {
        ImageJob job = requireJob(id);
        if (!TaskStatus.PENDING.getCode().equals(job.getStatus())
                && !TaskStatus.RUNNING.getCode().equals(job.getStatus())) {
            throw new IllegalStateException("only pending or running jobs can be canceled");
        }
        job.setStatus(TaskStatus.CANCELED.getCode());
        job.setProgress(Math.max(job.getProgress() == null ? 0 : job.getProgress(), 0));
        job.setErrorMessage(dto != null && StringUtils.hasText(dto.getErrorMessage())
                ? dto.getErrorMessage().trim()
                : "Image job canceled");
        job.setUpdateTime(now());
        return this.updateById(job);
    }

    private void submitToToolIfPossible(ImageJob job, boolean retry) {
        if (toolAdapterService == null) {
            markToolUnavailable(job, "Tool adapter service is not available: " + job.getToolCode());
            return;
        }

        try {
            ToolAdapterInfoDTO tool = toolAdapterService.getTool(job.getToolCode());
            if (tool == null || !tool.isConfigured()) {
                markToolUnavailable(job, "Tool adapter not configured: " + job.getToolCode());
                return;
            }

            ToolInvokeRequestDTO request = new ToolInvokeRequestDTO();
            request.setOperation(StringUtils.hasText(tool.getDefaultOperation())
                    ? tool.getDefaultOperation()
                    : "image-generate");
            request.setPayload(readMap(job.getInputJson()));

            ToolInvokeResponseDTO response = toolAdapterService.invoke(job.getToolCode(), request);
            job.setExternalJobId(extractExternalJobId(response));
            if (StringUtils.hasText(job.getExternalJobId())) {
                job.setStatus(TaskStatus.RUNNING.getCode());
                job.setErrorMessage(null);
            } else {
                job.setStatus(TaskStatus.FAILED.getCode());
                job.setErrorMessage("Tool adapter response did not include an external job id");
            }
            job.setProgress(0);
            job.setUpdateTime(now());
        } catch (UnsupportedOperationException | IllegalStateException e) {
            markToolUnavailable(job, e.getMessage());
        } catch (RuntimeException e) {
            if (retry) {
                job.setStatus(TaskStatus.FAILED.getCode());
            } else {
                job.setStatus(TaskStatus.CANCELED.getCode());
            }
            job.setErrorMessage(e.getMessage());
            job.setUpdateTime(now());
        }
    }

    private void markToolUnavailable(ImageJob job, String reason) {
        job.setStatus(TaskStatus.CANCELED.getCode());
        job.setProgress(0);
        job.setExternalJobId(null);
        job.setErrorMessage(reason);
        job.setUpdateTime(now());
    }

    private String extractExternalJobId(ToolInvokeResponseDTO response) {
        if (response == null || response.getBody() == null) {
            return null;
        }
        Object body = response.getBody();
        if (body instanceof Map<?, ?> map) {
            Object value = map.get("prompt_id");
            if (value == null) {
                value = map.get("promptId");
            }
            if (value == null) {
                value = map.get("job_id");
            }
            if (value == null) {
                value = map.get("jobId");
            }
            if (value == null) {
                value = map.get("externalJobId");
            }
            if (value == null) {
                value = map.get("id");
            }
            return value == null ? null : String.valueOf(value);
        }
        if (body instanceof String text && StringUtils.hasText(text)) {
            return text;
        }
        return response.getRawBody();
    }

    private ImageJob requireJob(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        ImageJob job = this.getById(id);
        if (job == null) {
            throw new IllegalStateException("image job not found: " + id);
        }
        return job;
    }

    private ImageJobDTO toDTO(ImageJob job) {
        ImageJobDTO dto = new ImageJobDTO();
        dto.setId(job.getId());
        dto.setTaskName(job.getTaskName());
        dto.setToolCode(job.getToolCode());
        dto.setInputData(readMap(job.getInputJson()));
        dto.setStatus(job.getStatus());
        dto.setProgress(job.getProgress());
        dto.setExternalJobId(job.getExternalJobId());
        dto.setErrorMessage(job.getErrorMessage());
        dto.setVisualPlanId(job.getVisualPlanId());
        dto.setSlot(job.getSlot());
        dto.setRatio(job.getRatio());
        dto.setPromptVersion(job.getPromptVersion());
        dto.setModelProfileId(job.getModelProfileId());
        dto.setSourceSnapshotJson(job.getSourceSnapshotJson());
        dto.setCreateTime(job.getCreateTime());
        dto.setUpdateTime(job.getUpdateTime());
        return dto;
    }

    private String normalizeStatus(String status) {
        if (!TaskStatus.isValid(status == null ? null : status.trim().toUpperCase())) {
            throw new IllegalArgumentException("unsupported image job status: " + status);
        }
        return status.trim().toUpperCase();
    }

    private boolean isTerminalStatus(String status) {
        return TaskStatus.SUCCEEDED.getCode().equals(status)
                || TaskStatus.FAILED.getCode().equals(status)
                || TaskStatus.CANCELED.getCode().equals(status);
    }

    private String requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (Exception e) {
            throw new IllegalStateException("failed to serialize image job json", e);
        }
    }

    private Map<String, Object> readMap(String json) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("failed to parse image job json", e);
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    @Override
    public List<ImageJobDTO> listByVisualPlanId(Long visualPlanId) {
        if (visualPlanId == null) {
            throw new IllegalArgumentException("visualPlanId must not be null");
        }
        LambdaQueryWrapper<ImageJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImageJob::getVisualPlanId, visualPlanId);
        wrapper.orderByAsc(ImageJob::getId);
        return this.list(wrapper).stream().map(this::toDTO).toList();
    }
}
