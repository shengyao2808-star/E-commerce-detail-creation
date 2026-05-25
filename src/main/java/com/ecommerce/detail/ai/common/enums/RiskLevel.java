package com.ecommerce.detail.ai.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 风险等级枚举
 * 
 * @author Administrator
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum RiskLevel {

    /**
     * 低风险
     */
    LOW(1, "低风险", "内容基本合规，风险较低"),

    /**
     * 中风险
     */
    MEDIUM(2, "中风险", "存在一定风险，需要人工审核"),

    /**
     * 高风险
     */
    HIGH(3, "高风险", "存在较高风险，建议修改"),

    /**
     * 极高风险
     */
    CRITICAL(4, "极高风险", "存在严重风险，必须修改");

    private final Integer code;
    private final String name;
    private final String description;

    public static RiskLevel getByCode(Integer code) {
        for (RiskLevel level : values()) {
            if (level.getCode().equals(code)) {
                return level;
            }
        }
        throw new IllegalArgumentException("未知的风险等级: " + code);
    }
}
