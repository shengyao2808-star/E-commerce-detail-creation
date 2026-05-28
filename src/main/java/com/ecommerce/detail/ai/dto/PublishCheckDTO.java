package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PublishCheckDTO {

    private Long id;

    private Long productDetailId;

    private String checkType;

    private String targetType;

    private String targetId;

    private String targetField;

    private String severity;

    private String status;

    private String message;

    private Object details;

    private Boolean overridden;

    private String overrideReason;

    private String overrideOperator;

    private LocalDateTime overrideTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
