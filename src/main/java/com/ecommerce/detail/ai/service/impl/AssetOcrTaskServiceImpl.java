package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.AssetOcrTaskDTO;
import com.ecommerce.detail.ai.dto.AssetOcrTaskResultDTO;
import com.ecommerce.detail.ai.dto.AssetOcrTaskStatusDTO;
import com.ecommerce.detail.ai.entity.AssetOcrTask;
import com.ecommerce.detail.ai.mapper.AssetOcrTaskMapper;
import com.ecommerce.detail.ai.service.AssetOcrTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class AssetOcrTaskServiceImpl extends ServiceImpl<AssetOcrTaskMapper, AssetOcrTask> implements AssetOcrTaskService {
    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "PENDING", "RUNNING", "SUCCEEDED", "FAILED", "CANCELED"
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAssetOcrTask(AssetOcrTaskDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Task data must not be null");
        }

        AssetOcrTask task = new AssetOcrTask();
        task.setMaterialId(dto.getMaterialId());
        task.setAssetName(dto.getAssetName());
        task.setAssetType(dto.getAssetType());
        task.setLanguage(dto.getLanguage());
        task.setStatus(StringUtils.hasText(dto.getStatus()) ? normalizeStatus(dto.getStatus()) : "PENDING");
        task.setProgress(dto.getProgress() != null ? dto.getProgress() : 0);
        task.setOcrText(dto.getOcrText());
        task.setConfidence(dto.getConfidence());
        task.setErrorMessage(dto.getErrorMessage());
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        this.save(task);
        return task.getId();
    }

    @Override
    public AssetOcrTaskDTO getAssetOcrTaskById(Long id) {
        AssetOcrTask task = getTaskOrThrow(id);
        return toDTO(task);
    }

    @Override
    public PageResult<AssetOcrTaskDTO> listAssetOcrTasks(int pageNum, int pageSize, Long materialId, String status, String language) {
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        Page<AssetOcrTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AssetOcrTask> wrapper = new LambdaQueryWrapper<>();
        if (materialId != null) {
            wrapper.eq(AssetOcrTask::getMaterialId, materialId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(AssetOcrTask::getStatus, normalizeStatus(status));
        }
        if (StringUtils.hasText(language)) {
            wrapper.eq(AssetOcrTask::getLanguage, language.trim());
        }
        wrapper.orderByDesc(AssetOcrTask::getCreateTime);

        Page<AssetOcrTask> resultPage = this.page(page, wrapper);
        List<AssetOcrTaskDTO> records = resultPage.getRecords().stream().map(this::toDTO).toList();
        return new PageResult<>(resultPage.getTotal(), records, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAssetOcrTaskStatus(Long id, AssetOcrTaskStatusDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Status data must not be null");
        }
        AssetOcrTask task = getTaskOrThrow(id);
        if (StringUtils.hasText(dto.getStatus())) {
            task.setStatus(normalizeStatus(dto.getStatus()));
        }
        if (dto.getProgress() != null) {
            task.setProgress(dto.getProgress());
        }
        if (dto.getErrorMessage() != null) {
            task.setErrorMessage(dto.getErrorMessage());
        }
        task.setUpdateTime(LocalDateTime.now());
        return this.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAssetOcrTaskResult(Long id, AssetOcrTaskResultDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Result data must not be null");
        }
        AssetOcrTask task = getTaskOrThrow(id);
        task.setStatus("SUCCEEDED");
        task.setProgress(dto.getProgress() != null ? dto.getProgress() : 100);
        task.setOcrText(dto.getOcrText() != null ? dto.getOcrText() : "");
        task.setConfidence(dto.getConfidence() != null ? dto.getConfidence() : 0D);
        task.setErrorMessage(dto.getErrorMessage());
        task.setUpdateTime(LocalDateTime.now());
        return this.updateById(task);
    }

    private AssetOcrTask getTaskOrThrow(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Task id must not be null");
        }
        AssetOcrTask task = this.getById(id);
        if (task == null) {
            throw new RuntimeException("Asset OCR task not found: " + id);
        }
        return task;
    }

    private AssetOcrTaskDTO toDTO(AssetOcrTask task) {
        AssetOcrTaskDTO dto = new AssetOcrTaskDTO();
        dto.setId(task.getId());
        dto.setMaterialId(task.getMaterialId());
        dto.setAssetName(task.getAssetName());
        dto.setAssetType(task.getAssetType());
        dto.setLanguage(task.getLanguage());
        dto.setStatus(task.getStatus());
        dto.setProgress(task.getProgress());
        dto.setOcrText(task.getOcrText() != null ? task.getOcrText() : "");
        dto.setConfidence(task.getConfidence() != null ? task.getConfidence() : 0D);
        dto.setErrorMessage(task.getErrorMessage());
        dto.setCreateTime(task.getCreateTime());
        dto.setUpdateTime(task.getUpdateTime());
        return dto;
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("Status must not be blank");
        }
        String normalized = status.trim().toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported OCR task status: " + status);
        }
        return normalized;
    }
}
