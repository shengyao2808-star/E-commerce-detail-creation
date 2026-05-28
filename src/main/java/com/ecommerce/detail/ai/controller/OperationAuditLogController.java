package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.OperationAuditLogDTO;
import com.ecommerce.detail.ai.service.OperationAuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/audit-logs")
public class OperationAuditLogController {

    @Autowired
    private OperationAuditLogService auditLogService;

    @GetMapping("/list")
    public Result<PageResult<OperationAuditLogDTO>> listLogs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Long operatorId) {
        return Result.success(auditLogService.listLogs(pageNum, pageSize, action, targetType, operatorId));
    }

    @PostMapping
    public Result<Long> createLog(@RequestBody OperationAuditLogDTO dto) {
        return Result.success(auditLogService.createLog(dto));
    }
}