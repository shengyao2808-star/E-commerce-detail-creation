package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class VisualPlanBatchStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long visualPlanId;

    private String aggregatedStatus;

    private Integer totalJobs;

    private Integer succeededJobs;

    private Integer failedJobs;

    private Integer pendingJobs;

    private Integer runningJobs;

    private Integer canceledJobs;

    private List<Map<String, Object>> jobSummaries;
}