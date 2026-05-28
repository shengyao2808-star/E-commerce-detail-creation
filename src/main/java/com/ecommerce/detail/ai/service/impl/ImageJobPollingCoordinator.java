package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.detail.ai.common.enums.TaskStatus;
import com.ecommerce.detail.ai.dto.GenerationResultDTO;
import com.ecommerce.detail.ai.dto.tool.ToolAdapterInfoDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeRequestDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeResponseDTO;
import com.ecommerce.detail.ai.entity.ImageJob;
import com.ecommerce.detail.ai.mapper.ImageJobMapper;
import com.ecommerce.detail.ai.service.GenerationResultService;
import com.ecommerce.detail.ai.service.ToolAdapterService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ImageJobPollingCoordinator {

    private static final String HISTORY_OPERATION = "history";
    private static final String RESULT_VIEW_PATH = "/view";

    @Autowired(required = false)
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ImageJobMapper imageJobMapper;

    @Autowired
    private GenerationResultService generationResultService;

    @Autowired
    private ToolAdapterService toolAdapterService;

    @Scheduled(fixedDelayString = "${business.image-job.poll-fixed-delay-ms:30000}")
    public void pollImageJobs() {
        if (imageJobMapper == null || generationResultService == null || toolAdapterService == null) {
            return;
        }

        List<ImageJob> jobs = imageJobMapper.selectList(new LambdaQueryWrapper<ImageJob>()
                .eq(ImageJob::getStatus, TaskStatus.RUNNING.getCode())
                .isNull(ImageJob::getVisualPlanId)
                .isNotNull(ImageJob::getExternalJobId)
                .ne(ImageJob::getExternalJobId, "")
                .orderByAsc(ImageJob::getUpdateTime));

        for (ImageJob job : jobs) {
            pollSingleJob(job);
        }
    }

    @Scheduled(fixedDelayString = "${business.image-job.poll-fixed-delay-ms:30000}")
    public Set<Long> pollVisualPlanBatches() {
        if (imageJobMapper == null || generationResultService == null || toolAdapterService == null) {
            return Set.of();
        }

        List<ImageJob> jobs = imageJobMapper.selectList(new LambdaQueryWrapper<ImageJob>()
                .eq(ImageJob::getStatus, TaskStatus.RUNNING.getCode())
                .isNotNull(ImageJob::getVisualPlanId)
                .isNotNull(ImageJob::getExternalJobId)
                .ne(ImageJob::getExternalJobId, "")
                .orderByAsc(ImageJob::getVisualPlanId)
                .orderByAsc(ImageJob::getUpdateTime));

        Set<Long> affectedPlans = new HashSet<>();
        Set<Long> seenPlans = new HashSet<>();
        for (ImageJob job : jobs) {
            Long visualPlanId = job.getVisualPlanId();
            if (visualPlanId != null && seenPlans.add(visualPlanId)) {
                affectedPlans.addAll(pollVisualPlanBatch(visualPlanId));
            }
        }
        return affectedPlans;
    }

    /**
     * Poll all active jobs belonging to a specific visual plan batch.
     * Returns the set of visual plan IDs that had terminal state transitions,
     * so the caller can recompute batch aggregated status.
     */
    public Set<Long> pollVisualPlanBatch(Long visualPlanId) {
        if (visualPlanId == null || imageJobMapper == null) {
            return Set.of();
        }

        List<ImageJob> jobs = imageJobMapper.selectList(new LambdaQueryWrapper<ImageJob>()
                .eq(ImageJob::getVisualPlanId, visualPlanId)
                .eq(ImageJob::getStatus, TaskStatus.RUNNING.getCode())
                .isNotNull(ImageJob::getExternalJobId)
                .ne(ImageJob::getExternalJobId, "")
                .orderByAsc(ImageJob::getUpdateTime));

        Set<Long> affectedPlans = new HashSet<>();
        for (ImageJob job : jobs) {
            String beforeStatus = job.getStatus();
            pollSingleJob(job);
            if (!beforeStatus.equals(job.getStatus())) {
                affectedPlans.add(visualPlanId);
            }
        }
        return affectedPlans;
    }

    public void pollSingleJob(Long id) {
        if (id == null) {
            return;
        }
        ImageJob job = imageJobMapper.selectById(id);
        if (job != null) {
            pollSingleJob(job);
        }
    }

    private void pollSingleJob(ImageJob job) {
        if (job == null
                || !TaskStatus.RUNNING.getCode().equals(job.getStatus())
                || !StringUtils.hasText(job.getToolCode())
                || !StringUtils.hasText(job.getExternalJobId())) {
            return;
        }

        ToolAdapterInfoDTO tool;
        try {
            tool = toolAdapterService.getTool(job.getToolCode());
        } catch (RuntimeException ex) {
            cancelJob(job, ex.getMessage());
            return;
        }

        if (tool == null || !tool.isConfigured()) {
            cancelJob(job, "Tool adapter not configured: " + job.getToolCode());
            return;
        }

        try {
            ToolInvokeRequestDTO request = new ToolInvokeRequestDTO();
            request.setOperation(HISTORY_OPERATION);
            request.setPayload(Map.of("prompt_id", job.getExternalJobId()));

            ToolInvokeResponseDTO response = toolAdapterService.invoke(job.getToolCode(), request);
            HistorySnapshot snapshot = parseSnapshot(response, job.getExternalJobId());

            switch (snapshot.state()) {
                case RUNNING -> updateRunningJob(job, snapshot.progress());
                case SUCCEEDED -> persistSuccessfulResults(job, snapshot, toolAdapterService.getBaseUrl(job.getToolCode()));
                case FAILED, MALFORMED -> failJob(job, snapshot.message());
            }
        } catch (UnsupportedOperationException | IllegalStateException ex) {
            failJob(job, ex.getMessage());
        } catch (RuntimeException ex) {
            failJob(job, ex.getMessage());
        }
    }

    private void persistSuccessfulResults(ImageJob job, HistorySnapshot snapshot, String baseUrl) {
        List<GenerationResultDTO> results = buildResults(job, normalize(baseUrl), snapshot.images());
        if (results.isEmpty()) {
            failJob(job, "External job completed without output for " + job.getExternalJobId());
            return;
        }

        for (GenerationResultDTO result : results) {
            generationResultService.upsertGenerationResult(result);
        }

        job.setStatus(TaskStatus.SUCCEEDED.getCode());
        job.setProgress(100);
        job.setErrorMessage(null);
        job.setUpdateTime(now());
        imageJobMapper.updateById(job);
    }

    private void updateRunningJob(ImageJob job, Integer progress) {
        if (progress != null) {
            job.setProgress(Math.max(0, Math.min(100, progress)));
        }
        job.setUpdateTime(now());
        imageJobMapper.updateById(job);
    }

    private void failJob(ImageJob job, String reason) {
        job.setStatus(TaskStatus.FAILED.getCode());
        job.setErrorMessage(StringUtils.hasText(reason) ? reason : "Image job polling failed");
        job.setUpdateTime(now());
        imageJobMapper.updateById(job);
    }

    private void cancelJob(ImageJob job, String reason) {
        job.setStatus(TaskStatus.CANCELED.getCode());
        job.setErrorMessage(StringUtils.hasText(reason) ? reason : "Tool adapter unavailable");
        job.setUpdateTime(now());
        imageJobMapper.updateById(job);
    }

    private HistorySnapshot parseSnapshot(ToolInvokeResponseDTO response, String externalJobId) {
        Map<String, Object> entry = extractHistoryEntry(response == null ? null : response.getBody(), externalJobId);
        if (entry == null) {
            return HistorySnapshot.malformed("Tool adapter returned malformed or missing history entry for " + externalJobId);
        }

        Map<String, Object> status = childMap(entry, "status");
        if (status == null) {
            return HistorySnapshot.malformed("Tool adapter returned malformed history status for " + externalJobId);
        }

        String statusText = firstText(status, "status_str");
        if (!StringUtils.hasText(statusText)) {
            return HistorySnapshot.malformed("Tool adapter returned malformed history status_str for " + externalJobId);
        }

        Boolean completedValue = firstBoolean(status, "completed");
        if (completedValue == null) {
            return HistorySnapshot.malformed("Tool adapter returned malformed history completed flag for " + externalJobId);
        }
        boolean completed = completedValue;
        List<?> messages = requireList(status.get("messages"), "status.messages");
        String message = extractStatusMessage(messages);
        Integer progress = completed ? 100 : extractProgress(entry, status);
        List<Map<String, Object>> images = extractImages(entry, externalJobId);

        if (isFailedStatus(statusText, message)) {
            return HistorySnapshot.failed(StringUtils.hasText(message)
                    ? message
                    : "External job failed for " + externalJobId);
        }

        if (isRunningStatus(statusText, completed)) {
            return HistorySnapshot.running(progress);
        }

        if (isSuccessfulStatus(statusText, completed)) {
            if (images.isEmpty()) {
                return HistorySnapshot.failed("External job completed without output for " + externalJobId);
            }
            return HistorySnapshot.succeeded(images);
        }

        return HistorySnapshot.malformed("Tool adapter returned unsupported history status '" + statusText + "' for " + externalJobId);
    }

    private Map<String, Object> extractHistoryEntry(Object body, String externalJobId) {
        Map<String, Object> root = toStringMap(body);
        if (root == null || root.isEmpty()) {
            return null;
        }

        if (hasHistoryShape(root)) {
            return root;
        }

        Map<String, Object> nestedHistory = childMap(root, "history");
        if (nestedHistory != null) {
            if (hasHistoryShape(nestedHistory)) {
                return nestedHistory;
            }
            Map<String, Object> nestedEntry = childMap(nestedHistory, externalJobId);
            if (nestedEntry != null && hasHistoryShape(nestedEntry)) {
                return nestedEntry;
            }
        }

        Map<String, Object> directEntry = childMap(root, externalJobId);
        if (directEntry != null && hasHistoryShape(directEntry)) {
            return directEntry;
        }

        return null;
    }

    private boolean hasHistoryShape(Map<String, Object> entry) {
        return entry != null && childMap(entry, "status") != null;
    }

    private List<GenerationResultDTO> buildResults(ImageJob job, String baseUrl, List<Map<String, Object>> images) {
        Map<String, GenerationResultDTO> deduped = new LinkedHashMap<>();
        for (Map<String, Object> image : images) {
            String resultUrl = resolveResultUrl(baseUrl, image);
            if (!StringUtils.hasText(resultUrl)) {
                continue;
            }

            GenerationResultDTO dto = new GenerationResultDTO();
            dto.setImageJobId(job.getId());
            dto.setResultUrl(resultUrl);
            dto.setThumbnailUrl(resolveThumbnailUrl(baseUrl, image, resultUrl));
            dto.setPrompt(resolvePrompt(job));
            dto.setParams(readMap(job.getInputJson()));
            dto.setComplianceStatus(firstText(image, "complianceStatus", "compliance_status"));
            dto.setSelected(Boolean.FALSE);
            deduped.merge(resultUrl, dto, this::mergeResult);
        }
        return new ArrayList<>(deduped.values());
    }

    private GenerationResultDTO mergeResult(GenerationResultDTO current, GenerationResultDTO candidate) {
        boolean currentUsesFallbackThumbnail = StringUtils.hasText(current.getThumbnailUrl())
                && current.getThumbnailUrl().equals(current.getResultUrl());
        if ((!StringUtils.hasText(current.getThumbnailUrl()) || currentUsesFallbackThumbnail) && StringUtils.hasText(candidate.getThumbnailUrl())) {
            current.setThumbnailUrl(candidate.getThumbnailUrl());
        }
        if (!StringUtils.hasText(current.getPrompt()) && StringUtils.hasText(candidate.getPrompt())) {
            current.setPrompt(candidate.getPrompt());
        }
        if (current.getParams() == null && candidate.getParams() != null) {
            current.setParams(candidate.getParams());
        }
        if (!StringUtils.hasText(current.getComplianceStatus()) && StringUtils.hasText(candidate.getComplianceStatus())) {
            current.setComplianceStatus(candidate.getComplianceStatus());
        }
        return current;
    }

    private String resolvePrompt(ImageJob job) {
        Map<String, Object> input = readMap(job.getInputJson());
        String prompt = firstText(input, "prompt", "positivePrompt", "promptText");
        return StringUtils.hasText(prompt) ? prompt : null;
    }

    private String resolveResultUrl(String baseUrl, Map<String, Object> image) {
        String explicitUrl = firstText(image, "url", "resultUrl");
        if (StringUtils.hasText(explicitUrl)) {
            if (explicitUrl.startsWith("http://") || explicitUrl.startsWith("https://")) {
                return explicitUrl;
            }
            if (StringUtils.hasText(baseUrl)) {
                return trimRight(baseUrl, "/") + (explicitUrl.startsWith("/") ? explicitUrl : "/" + explicitUrl);
            }
            return null;
        }

        String filename = firstText(image, "filename");
        if (!StringUtils.hasText(filename) || !StringUtils.hasText(baseUrl)) {
            return null;
        }

        return trimRight(baseUrl, "/") + RESULT_VIEW_PATH
                + "?filename=" + encode(filename)
                + "&subfolder=" + encode(firstText(image, "subfolder"))
                + "&type=" + encode(firstText(image, "type"));
    }

    private String resolveThumbnailUrl(String baseUrl, Map<String, Object> image, String fallback) {
        String thumbnail = firstText(image, "thumbnailUrl", "thumbUrl", "previewUrl", "preview");
        if (StringUtils.hasText(thumbnail)) {
            if (thumbnail.startsWith("http://") || thumbnail.startsWith("https://")) {
                return thumbnail;
            }
            if (StringUtils.hasText(baseUrl)) {
                return trimRight(baseUrl, "/") + (thumbnail.startsWith("/") ? thumbnail : "/" + thumbnail);
            }
        }
        return fallback;
    }

    private List<Map<String, Object>> extractImages(Map<String, Object> entry, String externalJobId) {
        Map<String, Object> outputs = childMap(entry, "outputs");
        if (outputs == null || outputs.isEmpty()) {
            return List.of();
        }

        Map<String, Map<String, Object>> deduped = new LinkedHashMap<>();
        for (Object node : outputs.values()) {
            Map<String, Object> outputNode = toStringMap(node);
            if (outputNode == null) {
                continue;
            }
            Object imageNode = outputNode.get("images");
            if (!(imageNode instanceof Iterable<?> iterable)) {
                continue;
            }
            for (Object item : iterable) {
                Map<String, Object> image = toStringMap(item);
                if (!hasRealImageShape(image)) {
                    throw new IllegalStateException("Tool adapter returned malformed image payload for "
                            + externalJobId);
                }
                String marker = firstText(image, "url", "resultUrl", "filename");
                deduped.putIfAbsent(marker, image);
            }
        }

        if (deduped.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(deduped.values());
    }

    private boolean hasRealImageShape(Map<String, Object> image) {
        if (image == null) {
            return false;
        }
        String explicitUrl = firstText(image, "url", "resultUrl");
        if (StringUtils.hasText(explicitUrl)) {
            return true;
        }
        return StringUtils.hasText(firstText(image, "filename"))
                && image.containsKey("subfolder")
                && StringUtils.hasText(firstText(image, "type"));
    }

    private Map<String, Object> childMap(Map<String, Object> parent, String key) {
        return parent == null ? null : toStringMap(parent.get(key));
    }

    private Map<String, Object> toStringMap(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Map<?, ?> rawMap) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return result;
        }
        if (value instanceof String text && StringUtils.hasText(text)) {
            String trimmed = text.trim();
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                try {
                    return objectMapper.readValue(trimmed, new TypeReference<Map<String, Object>>() {
                    });
                } catch (Exception ignored) {
                    return null;
                }
            }
        }
        try {
            return objectMapper.convertValue(value, new TypeReference<Map<String, Object>>() {
            });
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private List<?> requireList(Object value, String fieldName) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> list) {
            return list;
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> items = new ArrayList<>();
            for (Object item : iterable) {
                items.add(item);
            }
            return items;
        }
        if (value instanceof String text && StringUtils.hasText(text)) {
            String trimmed = text.trim();
            if (trimmed.startsWith("[") || trimmed.startsWith("{")) {
                try {
                    return objectMapper.readValue(trimmed, new TypeReference<List<Object>>() {
                    });
                } catch (Exception ignored) {
                    // fall through
                }
            }
        }
        return List.of(value);
    }

    private Map<String, Object> readMap(String json) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    private String extractStatusMessage(List<?> messages) {
        List<String> parts = new ArrayList<>();
        for (Object message : messages) {
            String text = stringifyMessage(message);
            if (StringUtils.hasText(text)) {
                parts.add(text);
            }
        }
        return parts.isEmpty() ? null : String.join("; ", parts);
    }

    private String stringifyMessage(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text) {
            return text;
        }
        if (value instanceof Iterable<?> iterable) {
            List<String> parts = new ArrayList<>();
            for (Object item : iterable) {
                String part = stringifyMessage(item);
                if (StringUtils.hasText(part)) {
                    parts.add(part);
                }
            }
            return parts.isEmpty() ? null : String.join(" ", parts);
        }
        if (value instanceof Map<?, ?> rawMap) {
            Map<String, Object> map = toStringMap(rawMap);
            if (map == null || map.isEmpty()) {
                return null;
            }
            return firstText(map, "message", "error", "exception", "text");
        }
        return String.valueOf(value);
    }

    private Integer extractProgress(Map<String, Object> entry, Map<String, Object> status) {
        Integer progress = firstInteger(entry, "progress");
        if (progress != null) {
            return progress;
        }

        progress = firstInteger(status, "progress", "percent");
        if (progress != null) {
            return progress;
        }

        Integer completed = firstInteger(status, "completed");
        Integer total = firstInteger(status, "total");
        if (completed != null && total != null && total > 0) {
            return Math.max(0, Math.min(100, (completed * 100) / total));
        }

        return null;
    }

    private boolean isFailedStatus(String statusText, String message) {
        String normalized = normalizeStatus(statusText);
        return normalized.contains("fail")
                || normalized.contains("error")
                || normalized.contains("cancel")
                || normalized.contains("abort")
                || StringUtils.hasText(message);
    }

    private boolean isRunningStatus(String statusText, boolean completed) {
        if (completed) {
            return false;
        }
        String normalized = normalizeStatus(statusText);
        return normalized.contains("run")
                || normalized.contains("queue")
                || normalized.contains("pend")
                || normalized.contains("process")
                || normalized.contains("exec");
    }

    private boolean isSuccessfulStatus(String statusText, boolean completed) {
        if (!completed) {
            return false;
        }
        String normalized = normalizeStatus(statusText);
        return normalized.contains("success")
                || normalized.contains("complete")
                || normalized.contains("done")
                || normalized.contains("finish")
                || normalized.contains("succeed");
    }

    private String normalizeStatus(String statusText) {
        return statusText == null ? "" : statusText.trim().toLowerCase();
    }

    private String firstText(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null && StringUtils.hasText(String.valueOf(value))) {
                return String.valueOf(value);
            }
        }
        return null;
    }

    private Integer firstInteger(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            Integer parsed = parseInteger(value);
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private Boolean firstBoolean(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value instanceof Boolean bool) {
                return bool;
            }
            if (value instanceof String text && StringUtils.hasText(text)) {
                return Boolean.parseBoolean(text.trim());
            }
        }
        return null;
    }

    private Integer parseInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && StringUtils.hasText(text)) {
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String trimRight(String value, String suffix) {
        String result = value;
        while (result.endsWith(suffix)) {
            result = result.substring(0, result.length() - suffix.length());
        }
        return result;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    private enum SnapshotState {
        RUNNING,
        SUCCEEDED,
        FAILED,
        MALFORMED
    }

    private record HistorySnapshot(
            SnapshotState state,
            Integer progress,
            List<Map<String, Object>> images,
            String message
    ) {
        private static HistorySnapshot running(Integer progress) {
            return new HistorySnapshot(SnapshotState.RUNNING, progress, List.of(), null);
        }

        private static HistorySnapshot succeeded(List<Map<String, Object>> images) {
            return new HistorySnapshot(SnapshotState.SUCCEEDED, 100, images, null);
        }

        private static HistorySnapshot failed(String message) {
            return new HistorySnapshot(SnapshotState.FAILED, null, List.of(), message);
        }

        private static HistorySnapshot malformed(String message) {
            return new HistorySnapshot(SnapshotState.MALFORMED, null, List.of(), message);
        }
    }
}
