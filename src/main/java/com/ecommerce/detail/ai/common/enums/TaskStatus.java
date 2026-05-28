package com.ecommerce.detail.ai.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

/**
 * Shared task statuses.
 */
@Getter
@AllArgsConstructor
public enum TaskStatus {

    PENDING("PENDING"),
    RUNNING("RUNNING"),
    SUCCEEDED("SUCCEEDED"),
    FAILED("FAILED"),
    CANCELED("CANCELED");

    private static final Set<String> VALUES = Set.of(PENDING.code, RUNNING.code, SUCCEEDED.code, FAILED.code, CANCELED.code);

    private final String code;

    public static boolean isValid(String status) {
        return status != null && VALUES.contains(status);
    }

    public static String normalize(String status, String fallback) {
        return isValid(status) ? status : fallback;
    }
}
