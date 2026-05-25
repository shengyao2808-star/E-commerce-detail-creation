package com.ecommerce.detail.ai.aspect;

import com.ecommerce.detail.ai.entity.AuditRecord;
import com.ecommerce.detail.ai.mapper.AuditRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Audit aspect for recording key service operations without interrupting business flow.
 */
@Slf4j
@Aspect
@Component
public class AuditAspect {

    private final AuditRecordMapper auditRecordMapper;

    public AuditAspect(AuditRecordMapper auditRecordMapper) {
        this.auditRecordMapper = auditRecordMapper;
    }

    @Pointcut("execution(* com.ecommerce.detail.ai.service..*.generate*(..)) || " +
            "execution(* com.ecommerce.detail.ai.service..*.export*(..)) || " +
            "execution(* com.ecommerce.detail.ai.service..*.audit*(..))")
    public void auditPointcut() {
    }

    @After("auditPointcut()")
    public void recordAuditLog(JoinPoint joinPoint) {
        try {
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            Object[] args = joinPoint.getArgs();

            AuditRecord auditRecord = new AuditRecord();
            auditRecord.setProductDetailId(extractProductDetailId(args));
            auditRecord.setAuditType("SYSTEM_OPERATION");
            auditRecord.setAuditStatus(1);
            auditRecord.setRiskLevel(0);
            auditRecord.setRiskItems("[]");
            auditRecord.setAuditComment(className + "." + methodName + " args=" + Arrays.toString(args));
            auditRecord.setModificationSuggestions(null);
            auditRecord.setSubmitter("system");
            auditRecord.setSubmitTime(LocalDateTime.now());
            auditRecord.setAuditor("system");
            auditRecord.setAuditTime(LocalDateTime.now());
            auditRecord.setAuditDuration(0);
            auditRecord.setCreateTime(LocalDateTime.now());

            auditRecordMapper.insert(auditRecord);

            log.debug("Audit log recorded: {}.{}, auditor: {}", className, methodName, auditRecord.getAuditor());
        } catch (Exception e) {
            log.error("Failed to record audit log", e);
        }
    }

    private Long extractProductDetailId(Object[] args) {
        if (args == null || args.length == 0 || args[0] == null) {
            return 0L;
        }
        Object firstArg = args[0];
        if (firstArg instanceof Long id) {
            return id;
        }
        try {
            return Long.parseLong(String.valueOf(firstArg));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
