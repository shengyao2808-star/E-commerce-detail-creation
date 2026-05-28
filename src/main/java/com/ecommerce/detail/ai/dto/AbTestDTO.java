package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AbTestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String testName;

    private String categoryCode;

    private String variantA;

    private String variantB;

    private String status;

    private Long visualPlanIdA;

    private Long visualPlanIdB;

    private Map<String, Object> metricsA;

    private Map<String, Object> metricsB;

    private String winner;

    private LocalDateTime completedTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
