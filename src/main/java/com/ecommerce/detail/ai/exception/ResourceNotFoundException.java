package com.ecommerce.detail.ai.exception;

/**
 * 资源不存在异常
 * 当请求的资源不存在时抛出此异常
 * 
 * @author Administrator
 * @version 1.0.0
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
