package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.PromptTemplateCreateDTO;
import com.ecommerce.detail.ai.dto.PromptTemplateDTO;
import com.ecommerce.detail.ai.entity.PromptTemplate;
import com.ecommerce.detail.ai.mapper.PromptTemplateMapper;
import com.ecommerce.detail.ai.service.PromptTemplateService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PromptTemplateServiceImpl extends ServiceImpl<PromptTemplateMapper, PromptTemplate>
        implements PromptTemplateService {

    private static final TypeReference<List<String>> LIST_TYPE = new TypeReference<>() {};

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public PageResult<PromptTemplateDTO> listTemplates(int pageNum, int pageSize, String category,
                                                        String platform, String style, String source, String keyword) {
        pageNum = Math.max(pageNum, 1);
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        LambdaQueryWrapper<PromptTemplate> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(category)) {
            wrapper.eq(PromptTemplate::getCategory, category.trim().toUpperCase());
        }
        if (StringUtils.hasText(platform)) {
            wrapper.eq(PromptTemplate::getPlatform, platform.trim().toUpperCase());
        }
        if (StringUtils.hasText(style)) {
            wrapper.eq(PromptTemplate::getStyle, style.trim().toUpperCase());
        }
        if (StringUtils.hasText(source)) {
            wrapper.eq(PromptTemplate::getSource, source.trim().toUpperCase());
        }
        if (StringUtils.hasText(keyword)) {
            String kw = "%" + keyword.trim() + "%";
            wrapper.and(w -> w.like(PromptTemplate::getTemplateName, kw)
                    .or().like(PromptTemplate::getDescription, kw)
                    .or().like(PromptTemplate::getPositivePrompt, kw));
        }
        wrapper.orderByDesc(PromptTemplate::getUsageCount);

        Page<PromptTemplate> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<PromptTemplateDTO> records = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.success(records, pageNum, pageSize, page.getTotal());
    }

    @Override
    public PromptTemplateDTO getTemplateById(Long id) {
        return toDTO(requireTemplate(id));
    }

    @Override
    public PromptTemplateDTO createTemplate(PromptTemplateCreateDTO dto) {
        PromptTemplate entity = new EntityBuilder()
                .name(dto.getTemplateName())
                .category(dto.getCategory())
                .sceneType(dto.getSceneType())
                .platform(dto.getPlatform())
                .style(dto.getStyle())
                .positivePrompt(dto.getPositivePrompt())
                .negativePrompt(dto.getNegativePrompt())
                .styleTags(dto.getStyleTags())
                .constraints(dto.getConstraints())
                .description(dto.getDescription())
                .previewImageUrl(dto.getPreviewImageUrl())
                .source(defaultIfBlank(dto.getSource(), "CUSTOM"))
                .sourceRef(dto.getSourceRef())
                .language(defaultIfBlank(dto.getLanguage(), "zh-CN"))
                .author(dto.getAuthor())
                .tags(dto.getTags())
                .status("ACTIVE")
                .build();

        this.save(entity);
        return toDTO(entity);
    }

    @Override
    public PromptTemplateDTO updateTemplate(Long id, PromptTemplateCreateDTO dto) {
        PromptTemplate entity = requireTemplate(id);

        if (StringUtils.hasText(dto.getTemplateName())) {
            entity.setTemplateName(dto.getTemplateName());
        }
        if (StringUtils.hasText(dto.getCategory())) {
            entity.setCategory(dto.getCategory().toUpperCase());
        }
        if (dto.getSceneType() != null) {
            entity.setSceneType(dto.getSceneType());
        }
        if (dto.getPlatform() != null) {
            entity.setPlatform(StringUtils.hasText(dto.getPlatform()) ? dto.getPlatform().toUpperCase() : null);
        }
        if (dto.getStyle() != null) {
            entity.setStyle(StringUtils.hasText(dto.getStyle()) ? dto.getStyle().toUpperCase() : null);
        }
        if (StringUtils.hasText(dto.getPositivePrompt())) {
            entity.setPositivePrompt(dto.getPositivePrompt());
        }
        if (dto.getNegativePrompt() != null) {
            entity.setNegativePrompt(dto.getNegativePrompt());
        }
        if (dto.getStyleTags() != null) {
            entity.setStyleTagsJson(writeList(dto.getStyleTags()));
        }
        if (dto.getConstraints() != null) {
            entity.setConstraintsJson(writeList(dto.getConstraints()));
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getPreviewImageUrl() != null) {
            entity.setPreviewImageUrl(dto.getPreviewImageUrl());
        }
        if (dto.getTags() != null) {
            entity.setTagsJson(writeList(dto.getTags()));
        }

        this.updateById(entity);
        return toDTO(entity);
    }

    @Override
    public void deleteTemplate(Long id) {
        requireTemplate(id);
        this.removeById(id);
    }

    @Override
    public PromptTemplateDTO duplicateTemplate(Long id) {
        PromptTemplate original = requireTemplate(id);
        PromptTemplate copy = new PromptTemplate();
        copy.setTemplateName(original.getTemplateName() + " (copy)");
        copy.setCategory(original.getCategory());
        copy.setSceneType(original.getSceneType());
        copy.setPlatform(original.getPlatform());
        copy.setStyle(original.getStyle());
        copy.setPositivePrompt(original.getPositivePrompt());
        copy.setNegativePrompt(original.getNegativePrompt());
        copy.setStyleTagsJson(original.getStyleTagsJson());
        copy.setConstraintsJson(original.getConstraintsJson());
        copy.setDescription(original.getDescription());
        copy.setPreviewImageUrl(original.getPreviewImageUrl());
        copy.setUsageCount(0);
        copy.setRating(BigDecimal.ZERO);
        copy.setSource("CUSTOM");
        copy.setSourceRef(null);
        copy.setLanguage(original.getLanguage());
        copy.setAuthor(original.getAuthor());
        copy.setTagsJson(original.getTagsJson());
        copy.setStatus("ACTIVE");

        this.save(copy);
        return toDTO(copy);
    }

    @Override
    public void incrementUsageCount(Long id) {
        PromptTemplate entity = requireTemplate(id);
        entity.setUsageCount(entity.getUsageCount() == null ? 1 : entity.getUsageCount() + 1);
        this.updateById(entity);
    }

    private PromptTemplate requireTemplate(Long id) {
        PromptTemplate entity = this.getById(id);
        if (entity == null) {
            throw new IllegalArgumentException("Prompt template not found: " + id);
        }
        return entity;
    }

    private PromptTemplateDTO toDTO(PromptTemplate entity) {
        PromptTemplateDTO dto = new PromptTemplateDTO();
        dto.setId(entity.getId());
        dto.setTemplateName(entity.getTemplateName());
        dto.setCategory(entity.getCategory());
        dto.setSceneType(entity.getSceneType());
        dto.setPlatform(entity.getPlatform());
        dto.setStyle(entity.getStyle());
        dto.setPositivePrompt(entity.getPositivePrompt());
        dto.setNegativePrompt(entity.getNegativePrompt());
        dto.setStyleTags(readList(entity.getStyleTagsJson()));
        dto.setConstraints(readList(entity.getConstraintsJson()));
        dto.setDescription(entity.getDescription());
        dto.setPreviewImageUrl(entity.getPreviewImageUrl());
        dto.setUsageCount(entity.getUsageCount());
        dto.setRating(entity.getRating());
        dto.setSource(entity.getSource());
        dto.setSourceRef(entity.getSourceRef());
        dto.setLanguage(entity.getLanguage());
        dto.setAuthor(entity.getAuthor());
        dto.setTags(readList(entity.getTagsJson()));
        dto.setStatus(entity.getStatus());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        return dto;
    }

    private List<String> readList(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            List<String> result = objectMapper.readValue(json, LIST_TYPE);
            return result == null ? Collections.emptyList() : result;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String writeList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return null;
        }
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }

    private class EntityBuilder {
        private final PromptTemplate entity = new PromptTemplate();

        EntityBuilder name(String v) { entity.setTemplateName(v); return this; }
        EntityBuilder category(String v) { entity.setCategory(v != null ? v.toUpperCase() : null); return this; }
        EntityBuilder sceneType(String v) { entity.setSceneType(v); return this; }
        EntityBuilder platform(String v) { entity.setPlatform(v); return this; }
        EntityBuilder style(String v) { entity.setStyle(v); return this; }
        EntityBuilder positivePrompt(String v) { entity.setPositivePrompt(v); return this; }
        EntityBuilder negativePrompt(String v) { entity.setNegativePrompt(v); return this; }
        EntityBuilder styleTags(List<String> v) { entity.setStyleTagsJson(writeList(v)); return this; }
        EntityBuilder constraints(List<String> v) { entity.setConstraintsJson(writeList(v)); return this; }
        EntityBuilder description(String v) { entity.setDescription(v); return this; }
        EntityBuilder previewImageUrl(String v) { entity.setPreviewImageUrl(v); return this; }
        EntityBuilder source(String v) { entity.setSource(v); return this; }
        EntityBuilder sourceRef(String v) { entity.setSourceRef(v); return this; }
        EntityBuilder language(String v) { entity.setLanguage(v); return this; }
        EntityBuilder author(String v) { entity.setAuthor(v); return this; }
        EntityBuilder tags(List<String> v) { entity.setTagsJson(writeList(v)); return this; }
        EntityBuilder status(String v) { entity.setStatus(v); return this; }

        PromptTemplate build() {
            entity.setUsageCount(0);
            entity.setRating(BigDecimal.ZERO);
            return entity;
        }
    }
}