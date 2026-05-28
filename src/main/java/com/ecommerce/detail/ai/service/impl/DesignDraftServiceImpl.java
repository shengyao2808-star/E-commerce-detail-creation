package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.DesignDraftDTO;
import com.ecommerce.detail.ai.entity.DesignDraft;
import com.ecommerce.detail.ai.mapper.DesignDraftMapper;
import com.ecommerce.detail.ai.service.DesignDraftService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DesignDraftServiceImpl extends ServiceImpl<DesignDraftMapper, DesignDraft> implements DesignDraftService {
    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "PENDING", "RUNNING", "SUCCEEDED", "FAILED", "CANCELED"
    );

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDesignDraft(DesignDraftDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Draft data must not be null");
        }

        DesignDraft draft = new DesignDraft();
        draft.setProductDetailId(dto.getProductDetailId());
        draft.setProductMaterialId(dto.getProductMaterialId());
        draft.setDraftName(dto.getDraftName());
        draft.setSceneJson(dto.getSceneJson());
        draft.setSelectedAssetsJson(serializeSelectedAssets(dto.getSelectedAssets()));
        draft.setStatus(StringUtils.hasText(dto.getStatus()) ? normalizeStatus(dto.getStatus()) : "PENDING");
        draft.setCreateTime(LocalDateTime.now());
        draft.setUpdateTime(LocalDateTime.now());

        this.save(draft);
        return draft.getId();
    }

    @Override
    public DesignDraftDTO getDesignDraftById(Long id) {
        DesignDraft draft = getDraftOrThrow(id);
        return toDTO(draft);
    }

    @Override
    public PageResult<DesignDraftDTO> listDesignDrafts(int pageNum, int pageSize, Long productDetailId, Long productMaterialId, String status) {
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        Page<DesignDraft> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DesignDraft> wrapper = new LambdaQueryWrapper<>();
        if (productDetailId != null) {
            wrapper.eq(DesignDraft::getProductDetailId, productDetailId);
        }
        if (productMaterialId != null) {
            wrapper.eq(DesignDraft::getProductMaterialId, productMaterialId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(DesignDraft::getStatus, normalizeStatus(status));
        }
        wrapper.orderByDesc(DesignDraft::getCreateTime);

        Page<DesignDraft> resultPage = this.page(page, wrapper);
        List<DesignDraftDTO> records = resultPage.getRecords().stream().map(this::toDTO).toList();
        return new PageResult<>(resultPage.getTotal(), records, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDesignDraft(Long id, DesignDraftDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Draft data must not be null");
        }

        DesignDraft draft = getDraftOrThrow(id);
        if (dto.getProductDetailId() != null) {
            draft.setProductDetailId(dto.getProductDetailId());
        }
        if (dto.getProductMaterialId() != null) {
            draft.setProductMaterialId(dto.getProductMaterialId());
        }
        if (dto.getDraftName() != null) {
            draft.setDraftName(dto.getDraftName());
        }
        if (dto.getSceneJson() != null) {
            draft.setSceneJson(dto.getSceneJson());
        }
        if (dto.getSelectedAssets() != null) {
            draft.setSelectedAssetsJson(serializeSelectedAssets(dto.getSelectedAssets()));
        }
        if (StringUtils.hasText(dto.getStatus())) {
            draft.setStatus(normalizeStatus(dto.getStatus()));
        }
        draft.setUpdateTime(LocalDateTime.now());
        return this.updateById(draft);
    }

    private DesignDraft getDraftOrThrow(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Draft id must not be null");
        }
        DesignDraft draft = this.getById(id);
        if (draft == null) {
            throw new RuntimeException("Design draft not found: " + id);
        }
        return draft;
    }

    private DesignDraftDTO toDTO(DesignDraft draft) {
        DesignDraftDTO dto = new DesignDraftDTO();
        dto.setId(draft.getId());
        dto.setProductDetailId(draft.getProductDetailId());
        dto.setProductMaterialId(draft.getProductMaterialId());
        dto.setDraftName(draft.getDraftName());
        dto.setSceneJson(draft.getSceneJson());
        dto.setSelectedAssets(deserializeSelectedAssets(draft.getSelectedAssetsJson()));
        dto.setStatus(draft.getStatus());
        dto.setCreateTime(draft.getCreateTime());
        dto.setUpdateTime(draft.getUpdateTime());
        return dto;
    }

    private String serializeSelectedAssets(List<Map<String, Object>> selectedAssets) {
        if (selectedAssets == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(selectedAssets);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize selected assets", e);
        }
    }

    private List<Map<String, Object>> deserializeSelectedAssets(String selectedAssetsJson) {
        if (!StringUtils.hasText(selectedAssetsJson)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(selectedAssetsJson, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize selected assets", e);
        }
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("Status must not be blank");
        }
        String normalized = status.trim().toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported draft status: " + status);
        }
        return normalized;
    }
}
