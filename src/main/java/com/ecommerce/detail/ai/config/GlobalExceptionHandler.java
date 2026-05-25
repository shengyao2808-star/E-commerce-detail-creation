package com.ecommerce.detail.ai.config;

import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.exception.AIServiceException;
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
 * 全局异常处理器
 * 统一处理系统异常，返回标准化的错误响应
 * 
 * @author Administrator
 * @version 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return Result.error(500, "业务异常: " + e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        
        e.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            } else {
                errors.put("global", error.getDefaultMessage());
            }
        });
        
        log.warn("参数校验失败: {}", errors);
        
        return Result.error(400, "参数校验失败", errors);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleBindException(BindException e) {
        Map<String, String> errors = new HashMap<>();
        
        e.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            } else {
                errors.put("global", error.getDefaultMessage());
            }
        });
        
        log.warn("参数绑定失败: {}", errors);
        
        return Result.error(400, "参数绑定失败", errors);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return Result.error(400, "非法参数: " + e.getMessage());
    }

    /**
     * 处理文件操作异常
     */
    @ExceptionHandler(java.io.IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleIOException(java.io.IOException e) {
        log.error("文件操作异常: {}", e.getMessage(), e);
        return Result.error(500, "文件操作失败: " + e.getMessage());
    }

    /**
     * 处理不支持操作异常
     */
    @ExceptionHandler(UnsupportedOperationException.class)
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public Result<String> handleUnsupportedOperationException(UnsupportedOperationException e) {
        log.warn("功能未实现: {}", e.getMessage());
        return Result.error(501, "功能未实现: " + e.getMessage());
    }

    @ExceptionHandler(AIServiceException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Result<String> handleAIServiceException(AIServiceException e) {
        log.warn("AI relay call failed: {}", e.getMessage(), e);
        return Result.error(502, "AI relay call failed: " + e.getMessage());
    }

    /**
     * 处理资源不存在异常
     */
    @ExceptionHandler(com.ecommerce.detail.ai.exception.ToolAdapterException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Result<String> handleToolAdapterException(com.ecommerce.detail.ai.exception.ToolAdapterException e) {
        log.warn("Tool adapter call failed: {}", e.getMessage(), e);
        return Result.error(502, "Tool adapter call failed: " + e.getMessage());
    }

    @ExceptionHandler(com.ecommerce.detail.ai.exception.ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<String> handleResourceNotFoundException(com.ecommerce.detail.ai.exception.ResourceNotFoundException e) {
        log.warn("资源不存在: {}", e.getMessage());
        return Result.error(404, e.getMessage());
    }

    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(com.ecommerce.detail.ai.exception.PermissionDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<String> handlePermissionDeniedException(com.ecommerce.detail.ai.exception.PermissionDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(403, e.getMessage());
    }

    /**
     * 处理未知异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.error(500, "系统异常，请稍后重试");
    }
}
