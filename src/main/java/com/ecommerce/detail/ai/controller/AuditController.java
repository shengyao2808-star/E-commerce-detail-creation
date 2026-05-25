package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.AuditDTO;
import com.ecommerce.detail.ai.entity.AuditRecord;
import com.ecommerce.detail.ai.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 审核控制器
 * 
 * @author Administrator
 * @version 1.0.0
 */
@RestController
@RequestMapping("/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    /**
     * 提交审核
     * 
     * @param dto 审核DTO
     * @return 结果
     */
    @PostMapping("/submit")
    public Result<Long> submitAudit(@RequestBody AuditDTO dto) {
        Long id = auditService.submitAudit(dto);
        return Result.success(id);
    }

    @GetMapping("/{id}")
    public Result<AuditRecord> getAuditById(@PathVariable Long id) {
        AuditRecord record = auditService.getAuditById(id);
        return Result.success(record);
    }

    /**
     * 获取商品详情页的审核记录
     *
     * @param productDetailId 商品详情页ID
     * @return 结果
     */
    @GetMapping("/product/{productDetailId}")
    public Result<AuditRecord> getAuditByProductDetailId(@PathVariable Long productDetailId) {
        AuditRecord record = auditService.getAuditByProductDetailId(productDetailId);
        return Result.success(record);
    }

    @GetMapping("/list")
    public PageResult<AuditRecord> listAuditRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String auditor) {
        return auditService.listAuditRecords(pageNum, pageSize, status, auditor);
    }

    @PutMapping("/{id}/approve")
    public Result<Boolean> approveAudit(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        boolean result = auditService.approveAudit(id, resolveComment(body));
        return Result.success(result);
    }

    @PutMapping("/{id}/reject")
    public Result<Boolean> rejectAudit(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        boolean result = auditService.rejectAudit(id, resolveComment(body));
        return Result.success(result);
    }

    @PutMapping("/{id}/return")
    public Result<Boolean> returnAudit(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        boolean result = auditService.returnAudit(id, resolveComment(body));
        return Result.success(result);
    }

    @PutMapping("/{id}/withdraw")
    public Result<Boolean> withdrawAudit(@PathVariable Long id) {
        boolean result = auditService.withdrawAudit(id);
        return Result.success(result);
    }

    @PutMapping("/{id}/reaudit")
    public Result<Boolean> reaudit(@PathVariable Long id) {
        boolean result = auditService.reaudit(id);
        return Result.success(result);
    }

    private String resolveComment(Map<String, String> body) {
        if (body == null) {
            return "";
        }
        String comment = body.get("comment");
        if (comment != null) {
            return comment;
        }
        String auditComment = body.get("auditComment");
        return auditComment != null ? auditComment : "";
    }
}
