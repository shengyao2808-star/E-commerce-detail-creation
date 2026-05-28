package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PostProcessTaskCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long sourceGenerationResultId;

    private String sourceImagePath;

    private String toolCode;

    private String operation;

    private Object params;

    private String maskImagePath;

    private Integer targetWidth;

    private Integer targetHeight;

    private String outputRatio;
}
