package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class PostProcessTaskDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long sourceGenerationResultId;
    private String sourceImagePath;
    private String outputImagePath;
    private String toolCode;
    private String operation;
    private Map<String, Object> params;
    private Integer inputWidth;
    private Integer inputHeight;
    private Long inputFileSize;
    private String inputMimeType;
    private Integer outputWidth;
    private Integer outputHeight;
    private Long outputFileSize;
    private String outputMimeType;
    private List<Map<String, Object>> sourceChain;
    private String status;
    private Integer progress;
    private String errorMessage;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
