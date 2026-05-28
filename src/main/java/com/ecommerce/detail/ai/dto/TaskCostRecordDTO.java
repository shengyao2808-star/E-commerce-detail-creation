package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class TaskCostRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String taskType;
    private Long taskId;
    private String toolCode;
    private String modelCode;
    private Long durationMs;
    private Integer invokeCount;
    private BigDecimal costAmount;
    private String costCurrency;
    private String costSource;
    private Long costConfigId;
    private String externalReceiptId;
    private Long visualPlanId;
    private String batchId;
    private String notes;
}
