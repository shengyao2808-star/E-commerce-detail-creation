package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class ProductContentTaskRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productDetailId;

    private Long materialId;

    private Long brandTemplateId;

    private Long visualPlanId;

    private Long promptWorkbenchEntryId;

    private String taskName;

    private String toolCode;

    private Map<String, Object> inputData;
}
