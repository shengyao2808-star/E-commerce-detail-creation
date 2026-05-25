package com.ecommerce.detail.ai.exception;

/**
 * 数据校验异常
 * 当数据校验失败时抛出此异常
 * 
 * @author Administrator
 * @version 1.0.0
 */
public class DataValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataValidationException(String message) {
        super(message);
    }

    public DataValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
