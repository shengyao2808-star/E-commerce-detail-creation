package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.GenerationResultDTO;
import com.ecommerce.detail.ai.dto.GenerationResultListQuery;
import com.ecommerce.detail.ai.dto.GenerationResultSelectionDTO;
import com.ecommerce.detail.ai.entity.GenerationResult;
import com.ecommerce.detail.ai.mapper.GenerationResultMapper;
import com.ecommerce.detail.ai.service.GenerationResultService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class GenerationResultServiceImpl extends ServiceImpl<GenerationResultMapper, GenerationResult> implements GenerationResultService {

    @Autowired(required = false)
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PageResult<GenerationResultDTO> listGenerationResults(GenerationResultListQuery query) {
        GenerationResultListQuery safeQuery = query == null ? new GenerationResultListQuery() : query;
        int pageNum = Math.max(safeQuery.getPageNum() == null ? 1 : safeQuery.getPageNum(), 1);
        int pageSize = safeQuery.getPageSize() == null ? 20 : safeQuery.getPageSize();
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        LambdaQueryWrapper<GenerationResult> wrapper = new LambdaQueryWrapper<>();
        if (safeQuery.getImageJobId() != null) {
            wrapper.eq(GenerationResult::getImageJobId, safeQuery.getImageJobId());
        }
        if (safeQuery.getSelected() != null) {
            wrapper.eq(GenerationResult::getSelected, Boolean.TRUE.equals(safeQuery.getSelected()) ? 1 : 0);
        }
        if (StringUtils.hasText(safeQuery.getComplianceStatus())) {
            wrapper.eq(GenerationResult::getComplianceStatus, safeQuery.getComplianceStatus().trim());
        }
        wrapper.orderByDesc(GenerationResult::getCreateTime);

        Page<GenerationResult> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<GenerationResultDTO> records = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.success(records, pageNum, pageSize, page.getTotal());
    }

    @Override
    public GenerationResultDTO getGenerationResultById(Long id) {
        return toDTO(requireResult(id));
    }

    @Override
    public Long saveGenerationResult(GenerationResultDTO dto) {
        return upsertGenerationResult(dto);
    }

    @Override
    public Long upsertGenerationResult(GenerationResultDTO dto) {
        GenerationResult payload = fromDTO(dto);
        GenerationResult existing = findExisting(payload.getImageJobId(), payload.getResultUrl());
        if (existing == null) {
            payload.setCreateTime(now());
            payload.setUpdateTime(now());
            this.save(payload);
            return payload.getId();
        }

        if (StringUtils.hasText(payload.getThumbnailUrl())) {
            existing.setThumbnailUrl(payload.getThumbnailUrl());
        }
        if (StringUtils.hasText(payload.getPrompt())) {
            existing.setPrompt(payload.getPrompt());
        }
        if (hasRealParams(payload.getParamsJson())) {
            existing.setParamsJson(payload.getParamsJson());
        }
        if (StringUtils.hasText(payload.getComplianceStatus())) {
            existing.setComplianceStatus(payload.getComplianceStatus());
        }
        existing.setUpdateTime(now());
        this.updateById(existing);
        return existing.getId();
    }

    private GenerationResult fromDTO(GenerationResultDTO dto) {
        GenerationResult result = new GenerationResult();
        result.setImageJobId(requireId(dto == null ? null : dto.getImageJobId(), "imageJobId"));
        result.setResultUrl(requireText(dto == null ? null : dto.getResultUrl(), "resultUrl"));
        result.setThumbnailUrl(dto == null ? null : dto.getThumbnailUrl());
        result.setPrompt(dto == null ? null : dto.getPrompt());
        result.setParamsJson(writeJson(dto == null ? null : dto.getParams()));
        result.setComplianceStatus(dto == null ? null : dto.getComplianceStatus());
        result.setSelected(Boolean.TRUE.equals(dto == null ? null : dto.getSelected()) ? 1 : 0);
        return result;
    }

    @Override
    public boolean updateGenerationResultSelection(Long id, GenerationResultSelectionDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("selection data must not be null");
        }
        GenerationResult result = requireResult(id);
        result.setSelected(Boolean.TRUE.equals(dto.getSelected()) ? 1 : 0);
        result.setUpdateTime(now());
        return this.updateById(result);
    }

    private GenerationResult requireResult(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        GenerationResult result = this.getById(id);
        if (result == null) {
            throw new IllegalStateException("generation result not found: " + id);
        }
        return result;
    }

    private GenerationResult findExisting(Long imageJobId, String resultUrl) {
        if (imageJobId == null || !StringUtils.hasText(resultUrl)) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<GenerationResult>()
                .eq(GenerationResult::getImageJobId, imageJobId)
                .eq(GenerationResult::getResultUrl, resultUrl)
                .last("limit 1"));
    }

    private Long requireId(Long value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        return value;
    }

    private String requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private GenerationResultDTO toDTO(GenerationResult result) {
        GenerationResultDTO dto = new GenerationResultDTO();
        dto.setId(result.getId());
        dto.setImageJobId(result.getImageJobId());
        dto.setResultUrl(result.getResultUrl());
        dto.setThumbnailUrl(result.getThumbnailUrl());
        dto.setPrompt(result.getPrompt());
        dto.setParams(readMap(result.getParamsJson()));
        dto.setComplianceStatus(result.getComplianceStatus());
        dto.setSelected(result.getSelected() != null && result.getSelected() > 0);
        dto.setCreateTime(result.getCreateTime());
        dto.setUpdateTime(result.getUpdateTime());
        return dto;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (Exception e) {
            throw new IllegalStateException("failed to serialize generation result json", e);
        }
    }

    private boolean hasRealParams(String json) {
        return StringUtils.hasText(json) && !"{}".equals(json.trim());
    }

    private Map<String, Object> readMap(String json) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("failed to parse generation result json", e);
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now();
    }
}
