package com.ecommerce.detail.ai.exception;

/**
 * 文件操作异常
 * 当文件读写、上传、下载等操作失败时抛出此异常
 * 
 * @author Administrator
 * @version 1.0.0
 */
public class FileOperationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FileOperationException(String message) {
        super(message);
    }

    public FileOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
