package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.OperationAuditLogDTO;
import com.ecommerce.detail.ai.entity.OperationAuditLog;
import com.ecommerce.detail.ai.mapper.OperationAuditLogMapper;
import com.ecommerce.detail.ai.service.OperationAuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;

@Service
public class OperationAuditLogServiceImpl extends ServiceImpl<OperationAuditLogMapper, OperationAuditLog> implements OperationAuditLogService {

    @Override
    public PageResult<OperationAuditLogDTO> listLogs(int pageNum, int pageSize, String action, String targetType, Long operatorId) {
        pageNum = Math.max(pageNum, 1);
        if (pageSize < 1 || pageSize > 100) pageSize = 20;
        LambdaQueryWrapper<OperationAuditLog> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(action)) w.eq(OperationAuditLog::getAction, action.trim());
        if (StringUtils.hasText(targetType)) w.eq(OperationAuditLog::getTargetType, targetType.trim());
        if (operatorId != null) w.eq(OperationAuditLog::getOperatorId, operatorId);
        w.orderByDesc(OperationAuditLog::getCreateTime);
        Page<OperationAuditLog> page = this.page(new Page<>(pageNum, pageSize), w);
        return PageResult.success(page.getRecords().stream().map(this::toDTO).toList(), pageNum, pageSize, page.getTotal());
    }

    @Override
    public Long createLog(OperationAuditLogDTO dto) {
        OperationAuditLog log = new OperationAuditLog();
        log.setOperatorId(dto.getOperatorId());
        log.setOperatorName(dto.getOperatorName());
        log.setAction(dto.getAction());
        log.setTargetType(dto.getTargetType());
        log.setTargetId(dto.getTargetId());
        log.setDetailJson(dto.getDetailJson());
        log.setCreateTime(LocalDateTime.now());
        this.save(log);
        return log.getId();
    }

    private OperationAuditLogDTO toDTO(OperationAuditLog l) {
        OperationAuditLogDTO dto = new OperationAuditLogDTO();
        dto.setId(l.getId());
        dto.setOperatorId(l.getOperatorId());
        dto.setOperatorName(l.getOperatorName());
        dto.setAction(l.getAction());
        dto.setTargetType(l.getTargetType());
        dto.setTargetId(l.getTargetId());
        dto.setDetailJson(l.getDetailJson());
        dto.setCreateTime(l.getCreateTime());
        return dto;
    }
}