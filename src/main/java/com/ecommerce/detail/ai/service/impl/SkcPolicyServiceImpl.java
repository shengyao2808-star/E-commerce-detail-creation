package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.SkcPolicyDTO;
import com.ecommerce.detail.ai.entity.SkcPolicy;
import com.ecommerce.detail.ai.mapper.SkcPolicyMapper;
import com.ecommerce.detail.ai.service.SkcPolicyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SkcPolicyServiceImpl extends ServiceImpl<SkcPolicyMapper, SkcPolicy> implements SkcPolicyService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("DRAFT", "CONFIRMED", "ARCHIVED");
    private static final TypeReference<List<Map<String, Object>>> MAP_LIST_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public PageResult<SkcPolicyDTO> listSkcPolicies(int pageNum, int pageSize, String keyword, String status) {
        pageNum = Math.max(pageNum, 1);
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        LambdaQueryWrapper<SkcPolicy> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(q -> q.like(SkcPolicy::getPolicyName, keyword)
                    .or()
                    .like(SkcPolicy::getCategoryCode, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SkcPolicy::getStatus, normalizeStatus(status));
        }
        wrapper.orderByDesc(SkcPolicy::getUpdateTime);

        Page<SkcPolicy> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<SkcPolicyDTO> records = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.success(records, pageNum, pageSize, page.getTotal());
    }

    @Override
    public Long createSkcPolicy(SkcPolicyDTO dto) {
        SkcPolicy policy = new SkcPolicy();
        policy.setPolicyName(requireText(dto == null ? null : dto.getPolicyName(), "policyName"));
        policy.setCategoryCode(dto == null ? null : dto.getCategoryCode());
        policy.setColorCount(dto == null ? null : dto.getColorCount());
        policy.setSpecCount(dto == null ? null : dto.getSpecCount());
        policy.setColorsJson(writeJson(dto == null ? null : dto.getColors()));
        policy.setSpecsJson(writeJson(dto == null ? null : dto.getSpecs()));
        policy.setRenderMode(dto == null ? null : dto.getRenderMode());
        policy.setVariantDisplayMode(dto == null ? null : dto.getVariantDisplayMode());
        policy.setGenerationRulesJson(writeJson(dto == null ? null : dto.getGenerationRules()));
        policy.setStatus(dto != null && StringUtils.hasText(dto.getStatus()) ? normalizeStatus(dto.getStatus()) : "DRAFT");
        policy.setVersion(dto != null && dto.getVersion() != null ? dto.getVersion() : 1);
        policy.setCreateTime(LocalDateTime.now());
        policy.setUpdateTime(LocalDateTime.now());
        this.save(policy);
        return policy.getId();
    }

    @Override
    public SkcPolicyDTO getSkcPolicyById(Long id) {
        return toDTO(requirePolicy(id));
    }

    @Override
    public boolean updateSkcPolicy(Long id, SkcPolicyDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("policy data must not be null");
        }
        SkcPolicy policy = requirePolicy(id);
        if (dto.getPolicyName() != null) {
            policy.setPolicyName(requireText(dto.getPolicyName(), "policyName"));
        }
        if (dto.getCategoryCode() != null) {
            policy.setCategoryCode(dto.getCategoryCode());
        }
        if (dto.getColorCount() != null) {
            policy.setColorCount(dto.getColorCount());
        }
        if (dto.getSpecCount() != null) {
            policy.setSpecCount(dto.getSpecCount());
        }
        if (dto.getColors() != null) {
            policy.setColorsJson(writeJson(dto.getColors()));
        }
        if (dto.getSpecs() != null) {
            policy.setSpecsJson(writeJson(dto.getSpecs()));
        }
        if (dto.getRenderMode() != null) {
            policy.setRenderMode(dto.getRenderMode());
        }
        if (dto.getVariantDisplayMode() != null) {
            policy.setVariantDisplayMode(dto.getVariantDisplayMode());
        }
        if (dto.getGenerationRules() != null) {
            policy.setGenerationRulesJson(writeJson(dto.getGenerationRules()));
        }
        if (StringUtils.hasText(dto.getStatus())) {
            policy.setStatus(normalizeStatus(dto.getStatus()));
        }
        policy.setVersion(policy.getVersion() == null ? 1 : policy.getVersion() + 1);
        policy.setUpdateTime(LocalDateTime.now());
        return this.updateById(policy);
    }

    @Override
    public SkcPolicyDTO confirmSkcPolicy(Long id) {
        SkcPolicy policy = requirePolicy(id);
        if ("CONFIRMED".equals(policy.getStatus())) {
            return toDTO(policy);
        }
        if (!"DRAFT".equals(policy.getStatus())) {
            throw new IllegalStateException("only DRAFT skc policy can be confirmed: " + id);
        }
        policy.setStatus("CONFIRMED");
        policy.setVersion(policy.getVersion() == null ? 1 : policy.getVersion() + 1);
        policy.setUpdateTime(LocalDateTime.now());
        this.updateById(policy);
        return toDTO(policy);
    }

    private SkcPolicy requirePolicy(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        SkcPolicy policy = this.getById(id);
        if (policy == null) {
            throw new IllegalStateException("skc policy not found: " + id);
        }
        return policy;
    }

    private SkcPolicyDTO toDTO(SkcPolicy policy) {
        SkcPolicyDTO dto = new SkcPolicyDTO();
        dto.setId(policy.getId());
        dto.setPolicyName(policy.getPolicyName());
        dto.setCategoryCode(policy.getCategoryCode());
        dto.setColorCount(policy.getColorCount());
        dto.setSpecCount(policy.getSpecCount());
        dto.setColors(readMapList(policy.getColorsJson()));
        dto.setSpecs(readMapList(policy.getSpecsJson()));
        dto.setRenderMode(policy.getRenderMode());
        dto.setVariantDisplayMode(policy.getVariantDisplayMode());
        dto.setGenerationRules(readStringList(policy.getGenerationRulesJson()));
        dto.setStatus(policy.getStatus());
        dto.setVersion(policy.getVersion());
        dto.setCreateTime(policy.getCreateTime());
        dto.setUpdateTime(policy.getUpdateTime());
        return dto;
    }

    private String normalizeStatus(String status) {
        String normalized = requireText(status, "status").toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("unsupported skc policy status: " + status);
        }
        return normalized;
    }

    private String requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Collections.emptyList() : value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize skc policy json", e);
        }
    }

    private List<Map<String, Object>> readMapList(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, MAP_LIST_TYPE);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<String> readStringList(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, STRING_LIST_TYPE);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}