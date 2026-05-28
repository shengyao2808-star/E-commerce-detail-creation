package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.enums.TaskStatus;
import com.ecommerce.detail.ai.dto.PromptWorkbenchEntryDTO;
import com.ecommerce.detail.ai.dto.PromptWorkbenchRequestDTO;
import com.ecommerce.detail.ai.dto.tool.ToolAdapterInfoDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeRequestDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeResponseDTO;
import com.ecommerce.detail.ai.entity.PromptWorkbenchEntry;
import com.ecommerce.detail.ai.mapper.PromptWorkbenchEntryMapper;
import com.ecommerce.detail.ai.service.PromptWorkbenchService;
import com.ecommerce.detail.ai.service.ToolAdapterService;
import com.ecommerce.detail.ai.util.AIUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PromptWorkbenchServiceImpl extends ServiceImpl<PromptWorkbenchEntryMapper, PromptWorkbenchEntry>
        implements PromptWorkbenchService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AIUtil aiUtil;

    @Autowired(required = false)
    private ToolAdapterService toolAdapterService;

    @Override
    public PageResult<PromptWorkbenchEntryDTO> listPromptWorkbenchEntries(int pageNum, int pageSize, String entryType, String status) {
        pageNum = Math.max(pageNum, 1);
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        LambdaQueryWrapper<PromptWorkbenchEntry> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(entryType)) {
            wrapper.eq(PromptWorkbenchEntry::getEntryType, normalizeEntryType(entryType));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(PromptWorkbenchEntry::getStatus, normalizeStatus(status));
        }
        wrapper.orderByDesc(PromptWorkbenchEntry::getCreateTime);

        Page<PromptWorkbenchEntry> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<PromptWorkbenchEntryDTO> records = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.success(records, pageNum, pageSize, page.getTotal());
    }

    @Override
    public PromptWorkbenchEntryDTO getPromptWorkbenchEntryById(Long id) {
        return toDTO(requireEntry(id));
    }

    @Override
    public PromptWorkbenchEntryDTO createGuidedPrompt(PromptWorkbenchRequestDTO dto) {
        requirePromptWorkbenchInput(dto, "promptText");
        return runTextPrompt("GUIDED", "ai-relay", dto,
                "你是电商视觉规划提示词助手，请基于输入信息生成可执行的引导型提示词、负面提示词和注意事项。",
                this::buildGuidedPromptUserContent);
    }

    @Override
    public PromptWorkbenchEntryDTO expandPrompt(PromptWorkbenchRequestDTO dto) {
        requirePromptWorkbenchInput(dto, "promptText");
        return runTextPrompt("EXPAND", "ai-relay", dto,
                "你是电商视觉规划提示词助手，请把输入提示词扩展为更完整、可执行、可审计的版本。",
                this::buildExpandPromptUserContent);
    }

    @Override
    public PromptWorkbenchEntryDTO imageToPrompt(PromptWorkbenchRequestDTO dto) {
        PromptWorkbenchEntry entry = createBaseEntry("IMAGE_TO_PROMPT", "llava", dto);
        entry.setStatus(TaskStatus.PENDING.getCode());
        this.save(entry);

        try {
            ToolAdapterInfoDTO tool = toolAdapterService == null ? null : toolAdapterService.getTool("llava");
            if (tool == null || !tool.isConfigured()) {
                return finishUnavailable(entry, "Tool adapter not configured: llava");
            }

            Map<String, Object> input = readMap(entry.getInputJson());
            String imageUrl = stringValue(input.get("imageUrl"));
            if (!StringUtils.hasText(imageUrl)) {
                return finishFailed(entry, "imageUrl must not be blank");
            }

            ToolInvokeRequestDTO request = new ToolInvokeRequestDTO();
            request.setOperation("image-to-prompt");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("imageUrl", imageUrl);
            payload.put("language", stringValue(input.get("language"), "zh-CN"));
            payload.put("output", input.get("output") != null ? input.get("output") : List.of("caption", "styleTags", "positivePrompt", "negativePrompt"));
            request.setPayload(payload);

            ToolInvokeResponseDTO response = toolAdapterService.invoke("llava", request);
            entry.setStatus(TaskStatus.SUCCEEDED.getCode());
            entry.setErrorMessage(null);
            Map<String, Object> output = buildToolOutputSnapshot(response);
            entry.setOutputText(stringValue(output.get("text")));
            entry.setOutputJson(writeJson(output));
            entry.setUpdateTime(LocalDateTime.now());
            this.updateById(entry);
            return toDTO(entry);
        } catch (UnsupportedOperationException e) {
            return finishUnavailable(entry, e.getMessage());
        } catch (RuntimeException e) {
            return finishFailed(entry, e.getMessage());
        }
    }

    private PromptWorkbenchEntryDTO runTextPrompt(String entryType,
                                                  String toolCode,
                                                  PromptWorkbenchRequestDTO dto,
                                                  String systemPrompt,
                                                  java.util.function.Function<PromptWorkbenchRequestDTO, String> userPromptBuilder) {
        PromptWorkbenchEntry entry = createBaseEntry(entryType, toolCode, dto);
        entry.setStatus(TaskStatus.PENDING.getCode());
        this.save(entry);

        try {
            String response = aiUtil.relayText(systemPrompt, userPromptBuilder.apply(dto));
            entry.setStatus(TaskStatus.SUCCEEDED.getCode());
            entry.setErrorMessage(null);
            Map<String, Object> output = buildRelayOutputSnapshot(response, toolCode);
            entry.setOutputText(stringValue(output.get("text")));
            entry.setOutputJson(writeJson(output));
            entry.setUpdateTime(LocalDateTime.now());
            this.updateById(entry);
            return toDTO(entry);
        } catch (UnsupportedOperationException e) {
            return finishUnavailable(entry, e.getMessage());
        } catch (RuntimeException e) {
            return finishFailed(entry, e.getMessage());
        }
    }

    private String buildGuidedPromptUserContent(PromptWorkbenchRequestDTO dto) {
        StringBuilder builder = new StringBuilder();
        appendLine(builder, "taskName", dto == null ? null : dto.getTaskName());
        appendLine(builder, "categoryCode", dto == null ? null : dto.getCategoryCode());
        appendLine(builder, "promptText", dto == null ? null : dto.getPromptText());
        appendLine(builder, "imageUrl", dto == null ? null : dto.getImageUrl());
        appendLine(builder, "inputData", dto == null ? null : dto.getInputData());
        return builder.toString();
    }

    private String buildExpandPromptUserContent(PromptWorkbenchRequestDTO dto) {
        StringBuilder builder = new StringBuilder();
        appendLine(builder, "taskName", dto == null ? null : dto.getTaskName());
        appendLine(builder, "promptText", dto == null ? null : dto.getPromptText());
        appendLine(builder, "inputData", dto == null ? null : dto.getInputData());
        return builder.toString();
    }

    private PromptWorkbenchEntry createBaseEntry(String entryType, String toolCode, PromptWorkbenchRequestDTO dto) {
        PromptWorkbenchEntry entry = new PromptWorkbenchEntry();
        String normalizedEntryType = normalizeEntryType(entryType);
        String normalizedToolCode = requireText(toolCode, "toolCode");
        entry.setEntryType(normalizedEntryType);
        entry.setTaskName(dto == null ? null : dto.getTaskName());
        entry.setToolCode(normalizedToolCode);
        entry.setVersion(1);
        entry.setInputJson(writeJson(buildInputSnapshot(dto, normalizedEntryType, normalizedToolCode)));
        entry.setCreateTime(LocalDateTime.now());
        entry.setUpdateTime(LocalDateTime.now());
        return entry;
    }

    private Map<String, Object> buildInputSnapshot(PromptWorkbenchRequestDTO dto, String entryType, String toolCode) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("entryType", entryType);
        snapshot.put("toolCode", toolCode);
        if (dto == null) {
            return snapshot;
        }
        snapshot.put("taskName", dto.getTaskName());
        snapshot.put("productDetailId", dto.getProductDetailId());
        snapshot.put("productMaterialId", dto.getProductMaterialId());
        snapshot.put("categoryCode", dto.getCategoryCode());
        snapshot.put("promptText", dto.getPromptText());
        snapshot.put("imageUrl", dto.getImageUrl());
        snapshot.put("inputData", dto.getInputData());
        return snapshot;
    }

    private PromptWorkbenchEntryDTO finishUnavailable(PromptWorkbenchEntry entry, String reason) {
        entry.setStatus(TaskStatus.CANCELED.getCode());
        entry.setOutputText(null);
        entry.setOutputJson(null);
        entry.setErrorMessage(reason);
        entry.setUpdateTime(LocalDateTime.now());
        this.updateById(entry);
        return toDTO(entry);
    }

    private PromptWorkbenchEntryDTO finishFailed(PromptWorkbenchEntry entry, String reason) {
        entry.setStatus(TaskStatus.FAILED.getCode());
        entry.setOutputText(null);
        entry.setOutputJson(null);
        entry.setErrorMessage(reason);
        entry.setUpdateTime(LocalDateTime.now());
        this.updateById(entry);
        return toDTO(entry);
    }

    private PromptWorkbenchEntry requireEntry(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        PromptWorkbenchEntry entry = this.getById(id);
        if (entry == null) {
            throw new IllegalStateException("prompt workbench entry not found: " + id);
        }
        return entry;
    }

    private PromptWorkbenchEntryDTO toDTO(PromptWorkbenchEntry entry) {
        PromptWorkbenchEntryDTO dto = new PromptWorkbenchEntryDTO();
        dto.setId(entry.getId());
        dto.setEntryType(entry.getEntryType());
        dto.setTaskName(entry.getTaskName());
        dto.setToolCode(entry.getToolCode());
        dto.setStatus(entry.getStatus());
        dto.setVersion(entry.getVersion());
        dto.setInputData(readMap(entry.getInputJson()));
        Map<String, Object> outputData = readMap(entry.getOutputJson());
        dto.setOutputData(outputData);
        dto.setOutputText(entry.getOutputText());
        dto.setErrorMessage(entry.getErrorMessage());
        dto.setPositivePrompt(readBodyString(outputData, "positivePrompt", null));
        dto.setNegativePrompt(readBodyString(outputData, "negativePrompt", null));
        dto.setShotScript(readBodyString(outputData, "shotScript", null));
        dto.setComposition(readBodyString(outputData, "composition", null));
        dto.setLighting(readBodyString(outputData, "lighting", null));
        dto.setCamera(readBodyString(outputData, "camera", null));
        dto.setStyleTags(readBodyStringList(outputData, "styleTags"));
        dto.setSourceData(readSourceData(outputData));
        dto.setRiskWarnings(readRiskWarnings(outputData));
        dto.setCreateTime(entry.getCreateTime());
        dto.setUpdateTime(entry.getUpdateTime());
        return dto;
    }

    private void appendLine(StringBuilder builder, String key, Object value) {
        if (value == null) {
            return;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return;
        }
        builder.append(key).append(": ").append(text).append('\n');
    }

    private String normalizeStatus(String status) {
        String normalized = requireText(status, "status").toUpperCase();
        if (!TaskStatus.isValid(normalized)) {
            throw new IllegalArgumentException("unsupported prompt workbench status: " + status);
        }
        return normalized;
    }

    private String normalizeEntryType(String entryType) {
        if (!StringUtils.hasText(entryType)) {
            throw new IllegalArgumentException("entryType must not be blank");
        }
        return entryType.trim().toUpperCase();
    }

    private void requirePromptWorkbenchInput(PromptWorkbenchRequestDTO dto, String primaryFieldName) {
        if (dto == null) {
            throw new IllegalArgumentException(primaryFieldName + " or inputData must not be blank");
        }
        if (StringUtils.hasText(dto.getPromptText())) {
            return;
        }
        if (dto.getInputData() != null && !dto.getInputData().isEmpty()) {
            return;
        }
        throw new IllegalArgumentException(primaryFieldName + " or inputData must not be blank");
    }

    private String requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private Map<String, Object> readMap(String json) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            return Map.of();
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize prompt workbench json", e);
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String stringValue(Object value, String fallback) {
        String text = stringValue(value);
        return StringUtils.hasText(text) ? text : fallback;
    }

    private Map<String, Object> normalizeStandardBody(Object body, String fallbackText) {
        if (body instanceof Map<?, ?> bodyMap) {
            Map<String, Object> normalized = new LinkedHashMap<>();
            bodyMap.forEach((key, value) -> normalized.put(String.valueOf(key), value));
            return normalized;
        }
        Map<String, Object> normalized = new LinkedHashMap<>();
        String text = body instanceof String s && StringUtils.hasText(s) ? s : null;
        if (text == null && StringUtils.hasText(fallbackText)) {
            text = fallbackText;
        }
        if (text != null) {
            normalized.put("positivePrompt", text);
        }
        return normalized;
    }

    private Map<String, Object> buildRelayOutputSnapshot(String response, String toolCode) {
        Map<String, Object> output = new LinkedHashMap<>();
        Object parsedBody = readJsonValue(response);
        Map<String, Object> normalizedBody = normalizeStandardBody(parsedBody, response);
        output.put("toolCode", toolCode);
        output.put("text", selectPrimaryText(normalizedBody, response));
        output.put("body", normalizedBody);
        output.put("rawBody", response);
        output.put("source", Map.of(
                "sourceType", "AI_RELAY",
                "toolCode", toolCode
        ));
        output.put("riskWarnings", extractRiskWarnings(normalizedBody));
        return output;
    }

    private Map<String, Object> buildToolOutputSnapshot(ToolInvokeResponseDTO response) {
        Map<String, Object> output = new LinkedHashMap<>();
        Object body = response == null ? Map.of() : response.getBody();
        String rawBody = response == null ? null : response.getRawBody();
        Map<String, Object> normalizedBody = normalizeStandardBody(body, rawBody);
        output.put("toolCode", response == null ? null : response.getToolCode());
        output.put("operation", response == null ? null : response.getOperation());
        output.put("statusCode", response == null ? null : response.getStatusCode());
        output.put("body", normalizedBody);
        output.put("rawBody", rawBody);
        output.put("text", selectPrimaryText(normalizedBody, rawBody));
        output.put("source", Map.of(
                "sourceType", "TOOL_ADAPTER",
                "toolCode", response == null ? null : response.getToolCode(),
                "operation", response == null ? null : response.getOperation()
        ));
        output.put("riskWarnings", extractRiskWarnings(normalizedBody));
        return output;
    }

    private Object readJsonValue(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        try {
            return objectMapper.readValue(value, Object.class);
        } catch (Exception e) {
            return value;
        }
    }

    private String selectPrimaryText(Object body, String rawBody) {
        if (body instanceof Map<?, ?> bodyMap) {
            Object positivePrompt = bodyMap.get("positivePrompt");
            if (StringUtils.hasText(stringValue(positivePrompt))) {
                return stringValue(positivePrompt);
            }
            Object caption = bodyMap.get("caption");
            if (StringUtils.hasText(stringValue(caption))) {
                return stringValue(caption);
            }
            Object text = bodyMap.get("text");
            if (StringUtils.hasText(stringValue(text))) {
                return stringValue(text);
            }
        }
        return rawBody;
    }

    private List<String> extractRiskWarnings(Object body) {
        if (!(body instanceof Map<?, ?> bodyMap)) {
            return Collections.emptyList();
        }
        Object warnings = bodyMap.get("riskWarnings");
        if (!(warnings instanceof List<?> warningList) || warningList.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> normalized = new ArrayList<>();
        for (Object warning : warningList) {
            if (warning == null) {
                continue;
            }
            String text = String.valueOf(warning).trim();
            if (!text.isEmpty()) {
                normalized.add(text);
            }
        }
        return normalized;
    }

    private Map<String, Object> readSourceData(Map<String, Object> outputData) {
        Object source = outputData.get("source");
        if (source instanceof Map<?, ?> sourceMap) {
            Map<String, Object> normalized = new LinkedHashMap<>();
            sourceMap.forEach((key, value) -> normalized.put(String.valueOf(key), value));
            return normalized;
        }
        return Map.of();
    }

    private List<String> readRiskWarnings(Map<String, Object> outputData) {
        Object riskWarnings = outputData.get("riskWarnings");
        if (!(riskWarnings instanceof List<?> warningList) || warningList.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> normalized = new ArrayList<>();
        for (Object warning : warningList) {
            if (warning == null) {
                continue;
            }
            String text = String.valueOf(warning).trim();
            if (!text.isEmpty()) {
                normalized.add(text);
            }
        }
        return normalized;
    }

    private String readBodyString(Map<String, Object> outputData, String key, String fallback) {
        Object body = outputData.get("body");
        if (body instanceof Map<?, ?> bodyMap) {
            Object value = bodyMap.get(key);
            String text = stringValue(value);
            if (StringUtils.hasText(text)) {
                return text;
            }
        }
        return fallback;
    }

    private List<String> readBodyStringList(Map<String, Object> outputData, String key) {
        Object body = outputData.get("body");
        if (!(body instanceof Map<?, ?> bodyMap)) {
            return Collections.emptyList();
        }
        Object rawValue = bodyMap.get(key);
        if (!(rawValue instanceof List<?> rawList) || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> normalized = new ArrayList<>();
        for (Object item : rawList) {
            if (item == null) {
                continue;
            }
            String text = String.valueOf(item).trim();
            if (!text.isEmpty()) {
                normalized.add(text);
            }
        }
        return normalized;
    }
}
