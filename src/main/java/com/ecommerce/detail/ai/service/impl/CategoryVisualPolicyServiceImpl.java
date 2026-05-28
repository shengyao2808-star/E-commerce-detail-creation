package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.CategoryVisualPolicyDTO;
import com.ecommerce.detail.ai.entity.CategoryVisualPolicy;
import com.ecommerce.detail.ai.mapper.CategoryVisualPolicyMapper;
import com.ecommerce.detail.ai.service.CategoryVisualPolicyService;
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
public class CategoryVisualPolicyServiceImpl extends ServiceImpl<CategoryVisualPolicyMapper, CategoryVisualPolicy>
        implements CategoryVisualPolicyService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("DRAFT", "CONFIRMED", "ARCHIVED");
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public PageResult<CategoryVisualPolicyDTO> listCategoryVisualPolicies(int pageNum, int pageSize, String keyword, String status) {
        pageNum = Math.max(pageNum, 1);
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        LambdaQueryWrapper<CategoryVisualPolicy> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(q -> q.like(CategoryVisualPolicy::getCategoryCode, keyword)
                    .or()
                    .like(CategoryVisualPolicy::getCategoryName, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(CategoryVisualPolicy::getStatus, normalizeStatus(status));
        }
        wrapper.orderByDesc(CategoryVisualPolicy::getUpdateTime);

        Page<CategoryVisualPolicy> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<CategoryVisualPolicyDTO> records = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.success(records, pageNum, pageSize, page.getTotal());
    }

    @Override
    public Long createCategoryVisualPolicy(CategoryVisualPolicyDTO dto) {
        CategoryVisualPolicy policy = new CategoryVisualPolicy();
        policy.setCategoryCode(requireText(dto == null ? null : dto.getCategoryCode(), "categoryCode"));
        policy.setCategoryName(dto == null ? null : dto.getCategoryName());
        policy.setModelPolicy(dto == null ? null : dto.getModelPolicy());
        policy.setModelConsistencyLevel(dto == null ? null : dto.getModelConsistencyLevel());
        policy.setAllowedShotTypesJson(writeJson(dto == null ? null : dto.getAllowedShotTypes()));
        policy.setRequiredMainImagesJson(writeJson(dto == null ? null : dto.getRequiredMainImages()));
        policy.setDetailScreenCountRangeJson(writeJson(dto == null ? null : dto.getDetailScreenCountRange()));
        policy.setRiskRulesJson(writeJson(dto == null ? null : dto.getRiskRules()));
        policy.setStatus(dto != null && StringUtils.hasText(dto.getStatus()) ? normalizeStatus(dto.getStatus()) : "DRAFT");
        policy.setVersion(dto != null && dto.getVersion() != null ? dto.getVersion() : 1);
        policy.setCreateTime(LocalDateTime.now());
        policy.setUpdateTime(LocalDateTime.now());
        this.save(policy);
        return policy.getId();
    }

    @Override
    public CategoryVisualPolicyDTO getCategoryVisualPolicyById(Long id) {
        return toDTO(requirePolicy(id));
    }

    @Override
    public boolean updateCategoryVisualPolicy(Long id, CategoryVisualPolicyDTO dto) {
        CategoryVisualPolicy policy = requirePolicy(id);
        if (dto == null) {
            throw new IllegalArgumentException("policy data must not be null");
        }
        if (dto.getCategoryCode() != null) {
            policy.setCategoryCode(requireText(dto.getCategoryCode(), "categoryCode"));
        }
        if (dto.getCategoryName() != null) {
            policy.setCategoryName(dto.getCategoryName());
        }
        if (dto.getModelPolicy() != null) {
            policy.setModelPolicy(dto.getModelPolicy());
        }
        if (dto.getModelConsistencyLevel() != null) {
            policy.setModelConsistencyLevel(dto.getModelConsistencyLevel());
        }
        if (dto.getAllowedShotTypes() != null) {
            policy.setAllowedShotTypesJson(writeJson(dto.getAllowedShotTypes()));
        }
        if (dto.getRequiredMainImages() != null) {
            policy.setRequiredMainImagesJson(writeJson(dto.getRequiredMainImages()));
        }
        if (dto.getDetailScreenCountRange() != null) {
            policy.setDetailScreenCountRangeJson(writeJson(dto.getDetailScreenCountRange()));
        }
        if (dto.getRiskRules() != null) {
            policy.setRiskRulesJson(writeJson(dto.getRiskRules()));
        }
        if (StringUtils.hasText(dto.getStatus())) {
            policy.setStatus(normalizeStatus(dto.getStatus()));
        }
        policy.setVersion(policy.getVersion() == null ? 1 : policy.getVersion() + 1);
        policy.setUpdateTime(LocalDateTime.now());
        return this.updateById(policy);
    }

    @Override
    public CategoryVisualPolicyDTO confirmCategoryVisualPolicy(Long id) {
        CategoryVisualPolicy policy = requirePolicy(id);
        if ("CONFIRMED".equals(policy.getStatus())) {
            return toDTO(policy);
        }
        if (!"DRAFT".equals(policy.getStatus())) {
            throw new IllegalStateException("only DRAFT category visual policy can be confirmed: " + id);
        }
        policy.setStatus("CONFIRMED");
        policy.setVersion(policy.getVersion() == null ? 1 : policy.getVersion() + 1);
        policy.setUpdateTime(LocalDateTime.now());
        this.updateById(policy);
        return toDTO(policy);
    }

    private CategoryVisualPolicy requirePolicy(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        CategoryVisualPolicy policy = this.getById(id);
        if (policy == null) {
            throw new IllegalStateException("category visual policy not found: " + id);
        }
        return policy;
    }

    private CategoryVisualPolicyDTO toDTO(CategoryVisualPolicy policy) {
        CategoryVisualPolicyDTO dto = new CategoryVisualPolicyDTO();
        dto.setId(policy.getId());
        dto.setCategoryCode(policy.getCategoryCode());
        dto.setCategoryName(policy.getCategoryName());
        dto.setModelPolicy(policy.getModelPolicy());
        dto.setModelConsistencyLevel(policy.getModelConsistencyLevel());
        dto.setAllowedShotTypes(readList(policy.getAllowedShotTypesJson()));
        dto.setRequiredMainImages(readMap(policy.getRequiredMainImagesJson()));
        dto.setDetailScreenCountRange(readMap(policy.getDetailScreenCountRangeJson()));
        dto.setRiskRules(readList(policy.getRiskRulesJson()));
        dto.setStatus(policy.getStatus());
        dto.setVersion(policy.getVersion());
        dto.setCreateTime(policy.getCreateTime());
        dto.setUpdateTime(policy.getUpdateTime());
        return dto;
    }

    private String normalizeStatus(String status) {
        String normalized = requireText(status, "status").toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("unsupported category visual policy status: " + status);
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
            throw new IllegalStateException("failed to serialize category visual policy json", e);
        }
    }

    private List<String> readList(String json) {
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
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}