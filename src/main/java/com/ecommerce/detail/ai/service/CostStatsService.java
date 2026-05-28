package com.ecommerce.detail.ai.service;

import com.ecommerce.detail.ai.dto.CostStatsDTO;

public interface CostStatsService {

    CostStatsDTO getOverallStats();

    CostStatsDTO getStatsByTask(String taskType, Long taskId);

    CostStatsDTO getStatsByVisualPlan(Long visualPlanId);

    CostStatsDTO getStatsByBatch(String batchId);

    CostStatsDTO getStatsByTool(String toolCode);

    CostStatsDTO getStatsByModel(String modelCode);
}
