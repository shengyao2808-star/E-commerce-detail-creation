package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class CostStatsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String scope;
    private Long scopeId;
    private Integer totalJobs;
    private Integer succeededJobs;
    private Integer failedJobs;
    private Integer canceledJobs;
    private BigDecimal totalCost;
    private BigDecimal avgCostPerJob;
    private BigDecimal successCost;
    private BigDecimal failCost;
    private Map<String, BigDecimal> costByTool;
    private Map<String, BigDecimal> costByModel;
    private Map<String, Integer> countByStatus;
    private Map<String, BigDecimal> costBySource;
    private String costCurrency;
}
