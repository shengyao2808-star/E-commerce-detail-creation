package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品链接预读结果
 */
@Data
public class ProductLinkPreviewDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String originalUrl;
    private String resolvedUrl;
    private String host;
    private String platform;
    private String productName;
    private String category;
    private String rawCategoryPath;
    private String brandName;
    private String pageTitle;
    private Integer httpStatus;
    private Boolean fetched;
    private Boolean loginRequired;
    private String source;
    private String message;
}
