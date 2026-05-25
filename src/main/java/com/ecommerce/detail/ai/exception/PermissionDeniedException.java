package com.ecommerce.detail.ai.exception;

/**
 * 权限不足异常
 * 当用户权限不足时抛出此异常
 * 
 * @author Administrator
 * @version 1.0.0
 */
public class PermissionDeniedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PermissionDeniedException(String message) {
        super(message);
    }

    public PermissionDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
