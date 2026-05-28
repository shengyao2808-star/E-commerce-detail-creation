package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.CostStatsDTO;
import com.ecommerce.detail.ai.dto.TaskCostRecordDTO;
import com.ecommerce.detail.ai.service.CostStatsService;
import com.ecommerce.detail.ai.service.TaskCostRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cost-stats")
public class CostStatsController {

    @Autowired
    private CostStatsService costStatsService;

    @Autowired
    private TaskCostRecordService taskCostRecordService;

    @GetMapping("")
    public Result<CostStatsDTO> getOverallStats() {
        return Result.success(costStatsService.getOverallStats());
    }

    @GetMapping("/task")
    public Result<CostStatsDTO> getStatsByTask(
            @RequestParam String taskType,
            @RequestParam(required = false) Long taskId) {
        return Result.success(costStatsService.getStatsByTask(taskType, taskId));
    }

    @GetMapping("/visual-plan/{planId}")
    public Result<CostStatsDTO> getStatsByVisualPlan(@PathVariable Long planId) {
        return Result.success(costStatsService.getStatsByVisualPlan(planId));
    }

    @GetMapping("/batch")
    public Result<CostStatsDTO> getStatsByBatch(@RequestParam String batchId) {
        return Result.success(costStatsService.getStatsByBatch(batchId));
    }

    @GetMapping("/tool")
    public Result<CostStatsDTO> getStatsByTool(@RequestParam String toolCode) {
        return Result.success(costStatsService.getStatsByTool(toolCode));
    }

    @GetMapping("/model")
    public Result<CostStatsDTO> getStatsByModel(@RequestParam String modelCode) {
        return Result.success(costStatsService.getStatsByModel(modelCode));
    }

    @GetMapping("/records")
    public Result<PageResult<TaskCostRecordDTO>> listCostRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String toolCode,
            @RequestParam(required = false) String modelCode,
            @RequestParam(required = false) Long visualPlanId) {
        return Result.success(taskCostRecordService.listCostRecords(pageNum, pageSize, taskType, taskId, toolCode, modelCode, visualPlanId));
    }

    @PostMapping("/records")
    public Result<Long> createCostRecord(@RequestBody TaskCostRecordDTO dto) {
        return Result.success(taskCostRecordService.createCostRecord(dto));
    }

    @PutMapping("/records/{id}")
    public Result<Boolean> updateCostRecord(@PathVariable Long id, @RequestBody TaskCostRecordDTO dto) {
        return Result.success(taskCostRecordService.updateCostRecord(id, dto));
    }
}
