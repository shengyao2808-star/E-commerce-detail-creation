package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.VisualPlanConfirmDTO;
import com.ecommerce.detail.ai.dto.VisualPlanDTO;
import com.ecommerce.detail.ai.dto.ImageJobCreateDTO;
import com.ecommerce.detail.ai.dto.VisualPlanBatchStatusDTO;
import java.util.List;
import java.util.Map;
import com.ecommerce.detail.ai.service.VisualPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/visual-plans")
public class VisualPlanController {

    @Autowired
    private VisualPlanService visualPlanService;

    @GetMapping({"", "/list"})
    public Result<PageResult<VisualPlanDTO>> listVisualPlans(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long productDetailId,
            @RequestParam(required = false) String status) {
        return Result.success(visualPlanService.listVisualPlans(pageNum, pageSize, productDetailId, status));
    }

    @PostMapping("")
    public Result<Long> createVisualPlan(@RequestBody VisualPlanDTO dto) {
        return Result.success(visualPlanService.createVisualPlan(dto));
    }

    @GetMapping("/{id}")
    public Result<VisualPlanDTO> getVisualPlanById(@PathVariable Long id) {
        return Result.success(visualPlanService.getVisualPlanById(id));
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateVisualPlan(@PathVariable Long id, @RequestBody VisualPlanDTO dto) {
        return Result.success(visualPlanService.updateVisualPlan(id, dto));
    }

    @PostMapping("/{id}/confirm")
    public Result<VisualPlanDTO> confirmVisualPlan(@PathVariable Long id, @RequestBody(required = false) VisualPlanConfirmDTO dto) {
        return Result.success(visualPlanService.confirmVisualPlan(id, dto));
    }

    @PostMapping("/{id}/dispatch")
    public Result<List<Long>> dispatchFromVisualPlan(@PathVariable Long id, @RequestBody List<ImageJobCreateDTO> jobs) {
        return Result.success(visualPlanService.dispatchFromVisualPlan(id, jobs));
    }

    @GetMapping("/{id}/batch-status")
    public Result<VisualPlanBatchStatusDTO> getBatchStatus(@PathVariable Long id) {
        return Result.success(visualPlanService.getBatchStatus(id));
    }

    @PostMapping("/{id}/batch-retry")
    public Result<Integer> retryAllFailedJobs(@PathVariable Long id) {
        return Result.success(visualPlanService.retryAllFailedJobs(id));
    }

    @PostMapping("/{id}/batch-cancel")
    public Result<Integer> cancelBatch(@PathVariable Long id) {
        return Result.success(visualPlanService.cancelBatch(id));
    }

    @GetMapping("/{id}/batch-results")
    public Result<Map<String, Object>> getBatchResultsBySlot(
            @PathVariable Long id,
            @RequestParam(required = false) String slot) {
        return Result.success(visualPlanService.getBatchResultsBySlot(id, slot));
    }
}
