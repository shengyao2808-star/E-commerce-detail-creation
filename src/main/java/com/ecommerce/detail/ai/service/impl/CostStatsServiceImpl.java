package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.dto.CostStatsDTO;
import com.ecommerce.detail.ai.entity.TaskCostRecord;
import com.ecommerce.detail.ai.mapper.TaskCostRecordMapper;
import com.ecommerce.detail.ai.service.CostStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CostStatsServiceImpl extends ServiceImpl<TaskCostRecordMapper, TaskCostRecord> implements CostStatsService {

    @Override
    public CostStatsDTO getOverallStats() {
        List<TaskCostRecord> records = list(new LambdaQueryWrapper<>());
        return buildStats(records, "OVERALL", null);
    }

    @Override
    public CostStatsDTO getStatsByTask(String taskType, Long taskId) {
        LambdaQueryWrapper<TaskCostRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskCostRecord::getTaskType, taskType);
        if (taskId != null) {
            wrapper.eq(TaskCostRecord::getTaskId, taskId);
        }
        List<TaskCostRecord> records = list(wrapper);
        return buildStats(records, "TASK:" + taskType, taskId);
    }

    @Override
    public CostStatsDTO getStatsByVisualPlan(Long visualPlanId) {
        List<TaskCostRecord> records = list(
                new LambdaQueryWrapper<TaskCostRecord>().eq(TaskCostRecord::getVisualPlanId, visualPlanId));
        return buildStats(records, "VISUAL_PLAN", visualPlanId);
    }

    @Override
    public CostStatsDTO getStatsByBatch(String batchId) {
        List<TaskCostRecord> records = list(
                new LambdaQueryWrapper<TaskCostRecord>().eq(TaskCostRecord::getBatchId, batchId));
        return buildStats(records, "BATCH", null);
    }

    @Override
    public CostStatsDTO getStatsByTool(String toolCode) {
        List<TaskCostRecord> records = list(
                new LambdaQueryWrapper<TaskCostRecord>().eq(TaskCostRecord::getToolCode, toolCode));
        return buildStats(records, "TOOL", null);
    }

    @Override
    public CostStatsDTO getStatsByModel(String modelCode) {
        List<TaskCostRecord> records = list(
                new LambdaQueryWrapper<TaskCostRecord>().eq(TaskCostRecord::getModelCode, modelCode));
        return buildStats(records, "MODEL", null);
    }

    private CostStatsDTO buildStats(List<TaskCostRecord> records, String scope, Long scopeId) {
        CostStatsDTO stats = new CostStatsDTO();
        stats.setScope(scope);
        stats.setScopeId(scopeId);
        stats.setTotalJobs(records.size());

        int succeeded = 0, failed = 0, canceled = 0;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal successCost = BigDecimal.ZERO;
        BigDecimal failCost = BigDecimal.ZERO;
        Map<String, BigDecimal> costByTool = new HashMap<>();
        Map<String, BigDecimal> costByModel = new HashMap<>();
        Map<String, Integer> countByStatus = new HashMap<>();

        for (TaskCostRecord r : records) {
            String taskType = r.getTaskType() != null ? r.getTaskType() : "UNKNOWN";
            countByStatus.merge(taskType, 1, Integer::sum);

            BigDecimal amount = r.getCostAmount() != null ? r.getCostAmount() : BigDecimal.ZERO;
            totalCost = totalCost.add(amount);

            if ("SUCCEEDED".equals(r.getCostSource()) || "CONFIG".equals(r.getCostSource()) || "RECEIPT".equals(r.getCostSource())) {
                successCost = successCost.add(amount);
                succeeded++;
            } else if ("FAILED".equals(r.getCostSource())) {
                failCost = failCost.add(amount);
                failed++;
            } else {
                canceled++;
            }

            if (r.getToolCode() != null) {
                costByTool.merge(r.getToolCode(), amount, BigDecimal::add);
            }
            if (r.getModelCode() != null) {
                costByModel.merge(r.getModelCode(), amount, BigDecimal::add);
            }
        }

        stats.setTotalCost(totalCost);
        stats.setAvgCostPerJob(records.isEmpty() ? null :
                totalCost.divide(BigDecimal.valueOf(records.size()), 6, RoundingMode.HALF_UP));
        stats.setCostByTool(costByTool.isEmpty() ? null : costByTool);
        stats.setCostByModel(costByModel.isEmpty() ? null : costByModel);
        stats.setCountByStatus(countByStatus.isEmpty() ? null : countByStatus);

        // Set extended fields via DTO subclass or direct assignment if fields exist
        stats.setSuccessCost(successCost);
        stats.setFailCost(failCost);
        stats.setSucceededJobs(succeeded);
        stats.setFailedJobs(failed);
        stats.setCanceledJobs(canceled);

        return stats;
    }
}
