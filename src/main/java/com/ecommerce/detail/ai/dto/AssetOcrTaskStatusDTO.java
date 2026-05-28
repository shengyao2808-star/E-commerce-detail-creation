package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AssetOcrTaskStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String status;
    private Integer progress;
    private String errorMessage;
}
