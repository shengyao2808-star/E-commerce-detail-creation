package com.ecommerce.detail.ai.exception;

/**
 * Raised when the configured AI relay is reachable only through an invalid,
 * failed, timed-out, or malformed response.
 */
public class AIServiceException extends RuntimeException {

    public AIServiceException(String message) {
        super(message);
    }

    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
