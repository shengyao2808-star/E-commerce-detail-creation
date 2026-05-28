package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.TaskCostRecordDTO;
import com.ecommerce.detail.ai.entity.TaskCostRecord;
import com.ecommerce.detail.ai.mapper.TaskCostRecordMapper;
import com.ecommerce.detail.ai.service.TaskCostRecordService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TaskCostRecordServiceImpl extends ServiceImpl<TaskCostRecordMapper, TaskCostRecord> implements TaskCostRecordService {

    @Override
    public PageResult<TaskCostRecordDTO> listCostRecords(int pageNum, int pageSize, String taskType, Long taskId,
                                                          String toolCode, String modelCode, Long visualPlanId) {
        pageNum = Math.max(pageNum, 1);
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        LambdaQueryWrapper<TaskCostRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(taskType)) {
            wrapper.eq(TaskCostRecord::getTaskType, taskType.trim());
        }
        if (taskId != null) {
            wrapper.eq(TaskCostRecord::getTaskId, taskId);
        }
        if (StringUtils.hasText(toolCode)) {
            wrapper.eq(TaskCostRecord::getToolCode, toolCode.trim());
        }
        if (StringUtils.hasText(modelCode)) {
            wrapper.eq(TaskCostRecord::getModelCode, modelCode.trim());
        }
        if (visualPlanId != null) {
            wrapper.eq(TaskCostRecord::getVisualPlanId, visualPlanId);
        }
        wrapper.orderByDesc(TaskCostRecord::getCreateTime);

        Page<TaskCostRecord> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<TaskCostRecordDTO> records = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.success(records, pageNum, pageSize, page.getTotal());
    }

    @Override
    public Long createCostRecord(TaskCostRecordDTO dto) {
        TaskCostRecord entity = new TaskCostRecord();
        entity.setTaskType(requireText(dto.getTaskType(), "taskType"));
        entity.setTaskId(requireNotNull(dto.getTaskId(), "taskId"));
        entity.setToolCode(dto.getToolCode());
        entity.setModelCode(dto.getModelCode());
        entity.setDurationMs(dto.getDurationMs());
        entity.setInvokeCount(dto.getInvokeCount() != null ? dto.getInvokeCount() : 1);
        entity.setCostAmount(dto.getCostAmount());
        entity.setCostCurrency(dto.getCostCurrency() != null ? dto.getCostCurrency() : "USD");
        entity.setCostSource(dto.getCostSource());
        entity.setCostConfigId(dto.getCostConfigId());
        entity.setExternalReceiptId(dto.getExternalReceiptId());
        entity.setVisualPlanId(dto.getVisualPlanId());
        entity.setBatchId(dto.getBatchId());
        entity.setNotes(dto.getNotes());
        this.save(entity);
        return entity.getId();
    }

    @Override
    public TaskCostRecordDTO getCostRecordById(Long id) {
        TaskCostRecord entity = this.getById(id);
        return entity != null ? toDTO(entity) : null;
    }

    @Override
    public boolean updateCostRecord(Long id, TaskCostRecordDTO dto) {
        TaskCostRecord entity = this.getById(id);
        if (entity == null) {
            return false;
        }
        if (dto.getCostAmount() != null) entity.setCostAmount(dto.getCostAmount());
        if (dto.getCostSource() != null) entity.setCostSource(dto.getCostSource());
        if (dto.getCostCurrency() != null) entity.setCostCurrency(dto.getCostCurrency());
        if (dto.getDurationMs() != null) entity.setDurationMs(dto.getDurationMs());
        if (dto.getInvokeCount() != null) entity.setInvokeCount(dto.getInvokeCount());
        if (dto.getExternalReceiptId() != null) entity.setExternalReceiptId(dto.getExternalReceiptId());
        if (dto.getNotes() != null) entity.setNotes(dto.getNotes());
        return this.updateById(entity);
    }

    @Override
    public BigDecimal sumCostByVisualPlanId(Long visualPlanId) {
        return baseMapper.sumCostByVisualPlanId(visualPlanId);
    }

    private TaskCostRecordDTO toDTO(TaskCostRecord entity) {
        TaskCostRecordDTO dto = new TaskCostRecordDTO();
        dto.setId(entity.getId());
        dto.setTaskType(entity.getTaskType());
        dto.setTaskId(entity.getTaskId());
        dto.setToolCode(entity.getToolCode());
        dto.setModelCode(entity.getModelCode());
        dto.setDurationMs(entity.getDurationMs());
        dto.setInvokeCount(entity.getInvokeCount());
        dto.setCostAmount(entity.getCostAmount());
        dto.setCostCurrency(entity.getCostCurrency());
        dto.setCostSource(entity.getCostSource());
        dto.setCostConfigId(entity.getCostConfigId());
        dto.setExternalReceiptId(entity.getExternalReceiptId());
        dto.setVisualPlanId(entity.getVisualPlanId());
        dto.setBatchId(entity.getBatchId());
        dto.setNotes(entity.getNotes());
        return dto;
    }

    private String requireText(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private Long requireNotNull(Long value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }
}
