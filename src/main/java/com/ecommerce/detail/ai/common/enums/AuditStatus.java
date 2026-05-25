package com.ecommerce.detail.ai.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核状态枚举
 * 
 * @author Administrator
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum AuditStatus {

    /**
     * 待审核
     */
    PENDING(0, "待审核"),

    /**
     * 审核中
     */
    AUDITING(1, "审核中"),

    /**
     * 审核通过
     */
    APPROVED(2, "审核通过"),

    /**
     * 审核拒绝
     */
    REJECTED(3, "审核拒绝"),

    /**
     * 需要修改
     */
    NEED_MODIFY(4, "需要修改");

    private final Integer code;
    private final String name;

    public static AuditStatus getByCode(Integer code) {
        for (AuditStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的审核状态: " + code);
    }
}
