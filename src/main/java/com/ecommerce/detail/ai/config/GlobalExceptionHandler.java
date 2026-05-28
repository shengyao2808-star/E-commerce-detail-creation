package com.ecommerce.detail.ai.config;

import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.exception.AIServiceException;
import com.ecommerce.detail.ai.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler.
 * P5.3: all client-facing error messages are scrubbed of local absolute paths
 * via {@link SecurityUtil#scrubLocalPaths(String)}.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.error("Business exception: {}", e.getMessage(), e);
        return Result.error(500, "Business exception: " + SecurityUtil.scrubLocalPaths(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                errors.put(fieldError.getField(), SecurityUtil.scrubLocalPaths(fieldError.getDefaultMessage()));
            } else {
                errors.put("global", SecurityUtil.scrubLocalPaths(error.getDefaultMessage()));
            }
        });
        log.warn("Validation failed: {}", errors);
        return Result.error(400, "Validation failed", errors);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleBindException(BindException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                errors.put(fieldError.getField(), SecurityUtil.scrubLocalPaths(fieldError.getDefaultMessage()));
            } else {
                errors.put("global", SecurityUtil.scrubLocalPaths(error.getDefaultMessage()));
            }
        });
        log.warn("Bind failed: {}", errors);
        return Result.error(400, "Bind failed", errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());
        return Result.error(400, "Illegal argument: " + SecurityUtil.scrubLocalPaths(e.getMessage()));
    }

    @ExceptionHandler(java.io.IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleIOException(java.io.IOException e) {
        log.error("IO exception: {}", e.getMessage(), e);
        return Result.error(500, "File operation failed");
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public Result<String> handleUnsupportedOperationException(UnsupportedOperationException e) {
        log.warn("Unsupported operation: {}", e.getMessage());
        return Result.error(501, "Feature not implemented: " + SecurityUtil.scrubLocalPaths(e.getMessage()));
    }

    @ExceptionHandler(AIServiceException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Result<String> handleAIServiceException(AIServiceException e) {
        log.warn("AI relay call failed: {}", e.getMessage(), e);
        return Result.error(502, "AI relay call failed");
    }

    @ExceptionHandler(com.ecommerce.detail.ai.exception.ToolAdapterException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Result<String> handleToolAdapterException(com.ecommerce.detail.ai.exception.ToolAdapterException e) {
        log.warn("Tool adapter call failed: {}", e.getMessage(), e);
        return Result.error(502, "Tool adapter call failed: " + SecurityUtil.scrubLocalPaths(e.getMessage()));
    }

    @ExceptionHandler(com.ecommerce.detail.ai.exception.ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<String> handleResourceNotFoundException(com.ecommerce.detail.ai.exception.ResourceNotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        return Result.error(404, SecurityUtil.scrubLocalPaths(e.getMessage()));
    }

    @ExceptionHandler(com.ecommerce.detail.ai.exception.PermissionDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<String> handlePermissionDeniedException(com.ecommerce.detail.ai.exception.PermissionDeniedException e) {
        log.warn("Permission denied: {}", e.getMessage());
        return Result.error(403, SecurityUtil.scrubLocalPaths(e.getMessage()));
    }

    @ExceptionHandler(com.ecommerce.detail.ai.exception.FileOperationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleFileOperationException(com.ecommerce.detail.ai.exception.FileOperationException e) {
        log.error("File operation exception: {}", e.getMessage(), e);
        return Result.error(500, "File operation failed");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleException(Exception e) {
        log.error("System exception: {}", e.getMessage(), e);
        return Result.error(500, "System error, please try again later");
    }
}
