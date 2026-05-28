package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AssetOcrTaskDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long materialId;
    private String assetName;
    private String assetType;
    private String language;
    private String status;
    private Integer progress;
    private String ocrText;
    private Double confidence;
    private String errorMessage;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
