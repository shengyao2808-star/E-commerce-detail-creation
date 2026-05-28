package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.detail.ai.common.enums.TaskStatus;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeRequestDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeResponseDTO;
import com.ecommerce.detail.ai.entity.PostProcessTask;
import com.ecommerce.detail.ai.mapper.PostProcessTaskMapper;
import com.ecommerce.detail.ai.service.ToolAdapterService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PostProcessPollingCoordinator {

    @Autowired(required = false)
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private PostProcessTaskMapper postProcessTaskMapper;

    @Autowired(required = false)
    private ToolAdapterService toolAdapterService;

    @Scheduled(fixedDelayString = "${business.post-process.poll-fixed-delay-ms:30000}")
    public void pollRunningTasks() {
        if (postProcessTaskMapper == null || toolAdapterService == null) {
            return;
        }

        List<PostProcessTask> tasks = postProcessTaskMapper.selectList(
                new LambdaQueryWrapper<PostProcessTask>()
                        .eq(PostProcessTask::getStatus, TaskStatus.RUNNING.getCode())
                        .orderByAsc(PostProcessTask::getUpdateTime));

        for (PostProcessTask task : tasks) {
            pollSingleTask(task);
        }
    }

    private void pollSingleTask(PostProcessTask task) {
        try {
            ToolInvokeRequestDTO request = new ToolInvokeRequestDTO();
            request.setOperation("status");

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("taskId", task.getId());
            payload.put("toolCode", task.getToolCode());
            payload.put("operation", task.getOperation());
            if (StringUtils.hasText(task.getSourceImagePath())) {
                payload.put("sourceImagePath", task.getSourceImagePath());
            }
            request.setPayload(payload);

            ToolInvokeResponseDTO response = toolAdapterService.invoke(task.getToolCode(), request);
            if (response == null || response.getStatusCode() == null) {
                return;
            }

            int code = response.getStatusCode();
            if (code >= 200 && code < 300) {
                handleSuccessResponse(task, response);
            }
        } catch (UnsupportedOperationException ignored) {
            // Tool not configured; skip polling
        } catch (RuntimeException ignored) {
            // Transient error; will retry next cycle
        }
    }

    private void handleSuccessResponse(PostProcessTask task, ToolInvokeResponseDTO response) {
        Map<String, Object> body = extractBody(response);
        if (body == null) {
            return;
        }

        String status = String.valueOf(body.getOrDefault("status", ""));
        if ("SUCCEEDED".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
            task.setStatus(TaskStatus.SUCCEEDED.getCode());
            task.setProgress(100);
            extractOutputMetadata(task, body);
        } else if ("FAILED".equalsIgnoreCase(status)) {
            task.setStatus(TaskStatus.FAILED.getCode());
            task.setErrorMessage(String.valueOf(body.getOrDefault("error", "Tool reported failure")));
        } else if ("RUNNING".equalsIgnoreCase(status) || "IN_PROGRESS".equalsIgnoreCase(status)) {
            Object progressObj = body.get("progress");
            if (progressObj instanceof Number num) {
                task.setProgress(Math.min(Math.max(num.intValue(), 0), 99));
            }
        }
        task.setUpdateTime(java.time.LocalDateTime.now());
        postProcessTaskMapper.updateById(task);
    }

    private void extractOutputMetadata(PostProcessTask task, Map<String, Object> body) {
        Object outputPath = body.get("outputImagePath");
        if (outputPath instanceof String s && StringUtils.hasText(s)) {
            task.setOutputImagePath(s);
        }
        Object width = body.get("outputWidth");
        if (width instanceof Number n) {
            task.setOutputWidth(n.intValue());
        }
        Object height = body.get("outputHeight");
        if (height instanceof Number n) {
            task.setOutputHeight(n.intValue());
        }
        Object fileSize = body.get("outputFileSize");
        if (fileSize instanceof Number n) {
            task.setOutputFileSize(n.longValue());
        }
        Object mimeType = body.get("outputMimeType");
        if (mimeType instanceof String s) {
            task.setOutputMimeType(s);
        }
    }

    private Map<String, Object> extractBody(ToolInvokeResponseDTO response) {
        if (response.getBody() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) response.getBody();
            return map;
        }
        if (StringUtils.hasText(response.getRawBody()) && objectMapper != null) {
            try {
                return objectMapper.readValue(response.getRawBody(), new TypeReference<>() {});
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
