package com.ecommerce.detail.ai.exception;

/**
 * Raised when a configured third-party tool adapter is reachable but returns
 * an invalid response, a non-success status, or cannot complete the request.
 */
public class ToolAdapterException extends RuntimeException {

    public ToolAdapterException(String message) {
        super(message);
    }

    public ToolAdapterException(String message, Throwable cause) {
        super(message, cause);
    }
}
