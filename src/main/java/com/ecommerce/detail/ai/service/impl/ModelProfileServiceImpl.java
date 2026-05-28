package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.ModelProfileDTO;
import com.ecommerce.detail.ai.entity.ModelProfile;
import com.ecommerce.detail.ai.mapper.ModelProfileMapper;
import com.ecommerce.detail.ai.service.ModelProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class ModelProfileServiceImpl extends ServiceImpl<ModelProfileMapper, ModelProfile> implements ModelProfileService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("DRAFT", "CONFIRMED", "ARCHIVED");
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public PageResult<ModelProfileDTO> listModelProfiles(int pageNum, int pageSize, String keyword, String status) {
        pageNum = Math.max(pageNum, 1);
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        LambdaQueryWrapper<ModelProfile> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ModelProfile::getDisplayName, keyword);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(ModelProfile::getStatus, normalizeStatus(status));
        }
        wrapper.orderByDesc(ModelProfile::getUpdateTime);

        Page<ModelProfile> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<ModelProfileDTO> records = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.success(records, pageNum, pageSize, page.getTotal());
    }

    @Override
    public Long createModelProfile(ModelProfileDTO dto) {
        ModelProfile profile = new ModelProfile();
        profile.setDisplayName(requireText(dto == null ? null : dto.getDisplayName(), "displayName"));
        profile.setFrontImage(dto == null ? null : dto.getFrontImage());
        profile.setSideImage(dto == null ? null : dto.getSideImage());
        profile.setBackImage(dto == null ? null : dto.getBackImage());
        profile.setHeight(dto == null ? null : dto.getHeight());
        profile.setWeight(dto == null ? null : dto.getWeight());
        profile.setBust(dto == null ? null : dto.getBust());
        profile.setWaist(dto == null ? null : dto.getWaist());
        profile.setHip(dto == null ? null : dto.getHip());
        profile.setStyleTagsJson(writeJson(dto == null ? null : dto.getStyleTags()));
        profile.setCategoryScopesJson(writeJson(dto == null ? null : dto.getCategoryScopes()));
        profile.setAuthorizationStatus(dto == null ? null : dto.getAuthorizationStatus());
        profile.setStatus(dto != null && StringUtils.hasText(dto.getStatus()) ? normalizeStatus(dto.getStatus()) : "DRAFT");
        profile.setVersion(dto != null && dto.getVersion() != null ? dto.getVersion() : 1);
        profile.setCreateTime(LocalDateTime.now());
        profile.setUpdateTime(LocalDateTime.now());
        this.save(profile);
        return profile.getId();
    }

    @Override
    public ModelProfileDTO getModelProfileById(Long id) {
        return toDTO(requireProfile(id));
    }

    @Override
    public boolean updateModelProfile(Long id, ModelProfileDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("profile data must not be null");
        }
        ModelProfile profile = requireProfile(id);
        if (dto.getDisplayName() != null) {
            profile.setDisplayName(requireText(dto.getDisplayName(), "displayName"));
        }
        if (dto.getFrontImage() != null) {
            profile.setFrontImage(dto.getFrontImage());
        }
        if (dto.getSideImage() != null) {
            profile.setSideImage(dto.getSideImage());
        }
        if (dto.getBackImage() != null) {
            profile.setBackImage(dto.getBackImage());
        }
        if (dto.getHeight() != null) {
            profile.setHeight(dto.getHeight());
        }
        if (dto.getWeight() != null) {
            profile.setWeight(dto.getWeight());
        }
        if (dto.getBust() != null) {
            profile.setBust(dto.getBust());
        }
        if (dto.getWaist() != null) {
            profile.setWaist(dto.getWaist());
        }
        if (dto.getHip() != null) {
            profile.setHip(dto.getHip());
        }
        if (dto.getStyleTags() != null) {
            profile.setStyleTagsJson(writeJson(dto.getStyleTags()));
        }
        if (dto.getCategoryScopes() != null) {
            profile.setCategoryScopesJson(writeJson(dto.getCategoryScopes()));
        }
        if (dto.getAuthorizationStatus() != null) {
            profile.setAuthorizationStatus(dto.getAuthorizationStatus());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            profile.setStatus(normalizeStatus(dto.getStatus()));
        }
        profile.setVersion(profile.getVersion() == null ? 1 : profile.getVersion() + 1);
        profile.setUpdateTime(LocalDateTime.now());
        return this.updateById(profile);
    }

    @Override
    public ModelProfileDTO confirmModelProfile(Long id) {
        ModelProfile profile = requireProfile(id);
        if ("CONFIRMED".equals(profile.getStatus())) {
            return toDTO(profile);
        }
        if (!"DRAFT".equals(profile.getStatus())) {
            throw new IllegalStateException("only DRAFT model profile can be confirmed: " + id);
        }
        profile.setStatus("CONFIRMED");
        profile.setVersion(profile.getVersion() == null ? 1 : profile.getVersion() + 1);
        profile.setUpdateTime(LocalDateTime.now());
        this.updateById(profile);
        return toDTO(profile);
    }

    private ModelProfile requireProfile(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        ModelProfile profile = this.getById(id);
        if (profile == null) {
            throw new IllegalStateException("model profile not found: " + id);
        }
        return profile;
    }

    private ModelProfileDTO toDTO(ModelProfile profile) {
        ModelProfileDTO dto = new ModelProfileDTO();
        dto.setId(profile.getId());
        dto.setDisplayName(profile.getDisplayName());
        dto.setFrontImage(profile.getFrontImage());
        dto.setSideImage(profile.getSideImage());
        dto.setBackImage(profile.getBackImage());
        dto.setHeight(profile.getHeight());
        dto.setWeight(profile.getWeight());
        dto.setBust(profile.getBust());
        dto.setWaist(profile.getWaist());
        dto.setHip(profile.getHip());
        dto.setStyleTags(readList(profile.getStyleTagsJson()));
        dto.setCategoryScopes(readList(profile.getCategoryScopesJson()));
        dto.setAuthorizationStatus(profile.getAuthorizationStatus());
        dto.setStatus(profile.getStatus());
        dto.setVersion(profile.getVersion());
        dto.setCreateTime(profile.getCreateTime());
        dto.setUpdateTime(profile.getUpdateTime());
        return dto;
    }

    private String normalizeStatus(String status) {
        String normalized = requireText(status, "status").toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("unsupported model profile status: " + status);
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
            throw new IllegalStateException("failed to serialize model profile json", e);
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
}