package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.enums.TaskStatus;
import com.ecommerce.detail.ai.dto.ProductContentTaskApplyDTO;
import com.ecommerce.detail.ai.dto.ProductContentTaskDTO;
import com.ecommerce.detail.ai.dto.ProductContentTaskRequestDTO;
import com.ecommerce.detail.ai.entity.ProductContentTask;
import com.ecommerce.detail.ai.entity.ProductDetail;
import com.ecommerce.detail.ai.mapper.ProductContentTaskMapper;
import com.ecommerce.detail.ai.mapper.ProductDetailMapper;
import com.ecommerce.detail.ai.service.ProductContentTaskService;
import com.ecommerce.detail.ai.util.AIUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProductContentTaskServiceImpl extends ServiceImpl<ProductContentTaskMapper, ProductContentTask>
        implements ProductContentTaskService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};
    private static final Set<String> APPLY_FIELDS = Set.of(
            "title", "subtitle", "sellingPoints", "seoKeywords", "description", "aiGeneratedContent"
    );

    @Autowired(required = false)
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ProductDetailMapper productDetailMapper;

    @Autowired
    private AIUtil aiUtil;

    @Override
    public PageResult<ProductContentTaskDTO> listProductContentTasks(int pageNum, int pageSize, Long productDetailId, String status) {
        pageNum = Math.max(pageNum, 1);
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        LambdaQueryWrapper<ProductContentTask> wrapper = new LambdaQueryWrapper<>();
        if (productDetailId != null) {
            wrapper.eq(ProductContentTask::getProductDetailId, productDetailId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(ProductContentTask::getStatus, normalizeStatus(status));
        }
        wrapper.orderByDesc(ProductContentTask::getCreateTime);

        Page<ProductContentTask> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<ProductContentTaskDTO> records = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.success(records, pageNum, pageSize, page.getTotal());
    }

    @Override
    public ProductContentTaskDTO getProductContentTaskById(Long id) {
        return toDTO(requireTask(id));
    }

    @Override
    public ProductContentTaskDTO createProductContentTask(ProductContentTaskRequestDTO dto) {
        ProductContentTask task = new ProductContentTask();
        task.setProductDetailId(requireProductDetailId(dto));
        task.setTaskName(StringUtils.hasText(dto.getTaskName()) ? dto.getTaskName().trim() : "Product content task");
        task.setToolCode(StringUtils.hasText(dto.getToolCode()) ? dto.getToolCode().trim() : "ai-relay");
        task.setStatus(TaskStatus.PENDING.getCode());
        task.setVersion(1);
        task.setInputJson(writeJson(buildInputSnapshot(dto)));
        task.setCreateTime(now());
        task.setUpdateTime(now());
        this.save(task);

        try {
            String response = aiUtil.relayText(buildSystemPrompt(), buildUserPrompt(dto));
            Map<String, Object> output = buildRelayOutputSnapshot(response, task.getToolCode());
            task.setStatus(TaskStatus.SUCCEEDED.getCode());
            task.setOutputText(response);
            task.setOutputJson(writeJson(output));
            task.setErrorMessage(null);
        } catch (UnsupportedOperationException e) {
            task.setStatus(TaskStatus.CANCELED.getCode());
            task.setOutputText(null);
            task.setOutputJson(null);
            task.setErrorMessage(e.getMessage());
        } catch (RuntimeException e) {
            task.setStatus(TaskStatus.FAILED.getCode());
            task.setOutputText(null);
            task.setOutputJson(null);
            task.setErrorMessage(e.getMessage());
        }
        task.setUpdateTime(now());
        this.updateById(task);
        return toDTO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductContentTaskDTO applyProductContentTask(Long id, ProductContentTaskApplyDTO dto) {
        ProductContentTask task = requireTask(id);
        if (!TaskStatus.SUCCEEDED.getCode().equals(task.getStatus())) {
            throw new IllegalStateException("only succeeded product content tasks can be applied");
        }
        ProductDetail detail = productDetailMapper.selectById(task.getProductDetailId());
        if (detail == null) {
            throw new IllegalStateException("product detail not found: " + task.getProductDetailId());
        }

        List<String> fields = normalizeApplyFields(dto == null ? null : dto.getFields());
        Map<String, Object> body = outputBody(task);
        for (String field : fields) {
            applyField(detail, field, body);
        }

        detail.setUpdateTime(now());
        productDetailMapper.updateById(detail);

        task.setAppliedFieldsJson(writeJson(fields));
        task.setAppliedTime(now());
        task.setUpdateTime(now());
        this.updateById(task);
        return toDTO(task);
    }

    private void applyField(ProductDetail detail, String field, Map<String, Object> body) {
        switch (field) {
            case "title" -> setText(body, "title", detail::setTitle);
            case "subtitle" -> setText(body, "subtitle", detail::setSubtitle);
            case "description" -> setText(body, "description", detail::setDescription);
            case "sellingPoints" -> detail.setSellingPoints(writeStringList(readStringList(body.get("sellingPoints"))));
            case "seoKeywords" -> detail.setSeoKeywords(writeStringList(readStringList(body.get("seoKeywords"))));
            case "aiGeneratedContent" -> detail.setAiGeneratedContent(writeJson(body));
            default -> throw new IllegalArgumentException("unsupported apply field: " + field);
        }
    }

    private void setText(Map<String, Object> body, String key, java.util.function.Consumer<String> setter) {
        String text = stringValue(body.get(key));
        if (StringUtils.hasText(text)) {
            setter.accept(text);
        }
    }

    private List<String> normalizeApplyFields(List<String> fields) {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("apply fields must not be empty");
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String field : fields) {
            if (!StringUtils.hasText(field)) {
                continue;
            }
            String trimmed = field.trim();
            if (!APPLY_FIELDS.contains(trimmed)) {
                throw new IllegalArgumentException("unsupported apply field: " + trimmed);
            }
            normalized.add(trimmed);
        }
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("apply fields must not be empty");
        }
        return new ArrayList<>(normalized);
    }

    private ProductContentTask requireTask(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        ProductContentTask task = this.getById(id);
        if (task == null) {
            throw new IllegalStateException("product content task not found: " + id);
        }
        return task;
    }

    private Long requireProductDetailId(ProductContentTaskRequestDTO dto) {
        if (dto == null || dto.getProductDetailId() == null) {
            throw new IllegalArgumentException("productDetailId must not be null");
        }
        return dto.getProductDetailId();
    }

    private Map<String, Object> buildInputSnapshot(ProductContentTaskRequestDTO dto) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("productDetailId", dto.getProductDetailId());
        snapshot.put("materialId", dto.getMaterialId());
        snapshot.put("brandTemplateId", dto.getBrandTemplateId());
        snapshot.put("visualPlanId", dto.getVisualPlanId());
        snapshot.put("promptWorkbenchEntryId", dto.getPromptWorkbenchEntryId());
        snapshot.put("taskName", dto.getTaskName());
        snapshot.put("toolCode", StringUtils.hasText(dto.getToolCode()) ? dto.getToolCode().trim() : "ai-relay");
        snapshot.put("inputData", dto.getInputData() == null ? Map.of() : dto.getInputData());
        return snapshot;
    }

    private String buildSystemPrompt() {
        return """
                You are an ecommerce product detail copy assistant.
                Return only valid JSON with fields:
                title, subtitle, sellingPoints, detailModules, faq, seoKeywords, riskWarnings.
                Do not invent specs, certifications, platform data, or performance claims not present in the input.
                """;
    }

    private String buildUserPrompt(ProductContentTaskRequestDTO dto) {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("productDetailId", dto.getProductDetailId());
        input.put("materialId", dto.getMaterialId());
        input.put("brandTemplateId", dto.getBrandTemplateId());
        input.put("visualPlanId", dto.getVisualPlanId());
        input.put("promptWorkbenchEntryId", dto.getPromptWorkbenchEntryId());
        input.put("taskName", dto.getTaskName());
        input.put("inputData", dto.getInputData() == null ? Map.of() : dto.getInputData());
        return writeJson(input);
    }

    private Map<String, Object> buildRelayOutputSnapshot(String response, String toolCode) {
        Object parsed = readJsonValue(response);
        if (!(parsed instanceof Map<?, ?> rawBody)) {
            throw new IllegalStateException("AI relay response must be structured JSON");
        }
        Map<String, Object> body = normalizeMap(rawBody);
        if (body.isEmpty()) {
            throw new IllegalStateException("AI relay response body must not be empty");
        }
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("toolCode", toolCode);
        output.put("body", body);
        output.put("rawBody", response);
        output.put("text", response);
        output.put("source", Map.of("sourceType", "AI_RELAY", "toolCode", toolCode));
        output.put("riskWarnings", readStringList(body.get("riskWarnings")));
        return output;
    }

    private ProductContentTaskDTO toDTO(ProductContentTask task) {
        ProductContentTaskDTO dto = new ProductContentTaskDTO();
        dto.setId(task.getId());
        dto.setProductDetailId(task.getProductDetailId());
        dto.setTaskName(task.getTaskName());
        dto.setToolCode(task.getToolCode());
        dto.setStatus(task.getStatus());
        dto.setVersion(task.getVersion());
        dto.setInputData(readMap(task.getInputJson()));
        Map<String, Object> output = readMap(task.getOutputJson());
        dto.setOutputData(output);
        dto.setOutputText(task.getOutputText());
        Map<String, Object> body = outputBody(output);
        dto.setTitle(stringValue(body.get("title")));
        dto.setSubtitle(stringValue(body.get("subtitle")));
        dto.setSellingPoints(readStringList(body.get("sellingPoints")));
        dto.setDetailModules(readMapList(body.get("detailModules")));
        dto.setFaq(readMapList(body.get("faq")));
        dto.setSeoKeywords(readStringList(body.get("seoKeywords")));
        dto.setRiskWarnings(readStringList(output.getOrDefault("riskWarnings", body.get("riskWarnings"))));
        dto.setSourceData(readSourceData(output));
        dto.setAppliedFields(readStoredStringList(task.getAppliedFieldsJson()));
        dto.setAppliedTime(task.getAppliedTime());
        dto.setErrorMessage(task.getErrorMessage());
        dto.setCreateTime(task.getCreateTime());
        dto.setUpdateTime(task.getUpdateTime());
        return dto;
    }

    private Map<String, Object> outputBody(ProductContentTask task) {
        return outputBody(readMap(task.getOutputJson()));
    }

    private Map<String, Object> outputBody(Map<String, Object> output) {
        Object body = output.get("body");
        if (body instanceof Map<?, ?> rawBody) {
            return normalizeMap(rawBody);
        }
        return Map.of();
    }

    private Map<String, Object> readSourceData(Map<String, Object> output) {
        Object source = output.get("source");
        if (source instanceof Map<?, ?> rawSource) {
            return normalizeMap(rawSource);
        }
        return Map.of();
    }

    private List<Map<String, Object>> readMapList(Object value) {
        if (!(value instanceof List<?> rawList)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> normalized = new ArrayList<>();
        for (Object item : rawList) {
            if (item instanceof Map<?, ?> rawMap) {
                normalized.add(normalizeMap(rawMap));
            }
        }
        return normalized;
    }

    private List<String> readStringList(Object value) {
        if (value instanceof List<?> rawList) {
            List<String> normalized = new ArrayList<>();
            for (Object item : rawList) {
                String text = stringValue(item);
                if (StringUtils.hasText(text)) {
                    normalized.add(text);
                }
            }
            return normalized;
        }
        String text = stringValue(value);
        if (!StringUtils.hasText(text)) {
            return Collections.emptyList();
        }
        List<String> values = new ArrayList<>();
        for (String item : text.split("[,\\n]")) {
            if (StringUtils.hasText(item)) {
                values.add(item.trim());
            }
        }
        return values;
    }

    private List<String> readStoredStringList(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, STRING_LIST_TYPE);
        } catch (Exception e) {
            return Collections.emptyList();
        }
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

    private Object readJsonValue(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return objectMapper.readValue(value, Object.class);
        } catch (Exception e) {
            throw new IllegalStateException("AI relay response must be valid JSON", e);
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize product content task json", e);
        }
    }

    private String writeStringList(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? Collections.emptyList() : values);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize product detail list", e);
        }
    }

    private Map<String, Object> normalizeMap(Map<?, ?> rawMap) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        rawMap.forEach((key, value) -> normalized.put(String.valueOf(key), value));
        return normalized;
    }

    private String normalizeStatus(String status) {
        String normalized = status == null ? null : status.trim().toUpperCase();
        if (!TaskStatus.isValid(normalized)) {
            throw new IllegalArgumentException("unsupported product content task status: " + status);
        }
        return normalized;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private LocalDateTime now() {
        return LocalDateTime.now();
    }
}
