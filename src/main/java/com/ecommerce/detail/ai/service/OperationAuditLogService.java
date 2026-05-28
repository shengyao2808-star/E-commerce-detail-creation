package com.ecommerce.detail.ai.service;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.OperationAuditLogDTO;

public interface OperationAuditLogService {
    PageResult<OperationAuditLogDTO> listLogs(int pageNum, int pageSize, String action, String targetType, Long operatorId);
    Long createLog(OperationAuditLogDTO dto);
}