package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AssetOcrTaskResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String ocrText;
    private Double confidence;
    private Integer progress;
    private String errorMessage;
}
