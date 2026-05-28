package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class PromptWorkbenchRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String taskName;

    private String entryType;

    private String toolCode;

    private Long productDetailId;

    private Long productMaterialId;

    private String categoryCode;

    private String promptText;

    private String imageUrl;

    private Map<String, Object> inputData;
}
