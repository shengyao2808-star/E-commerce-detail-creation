package com.ecommerce.detail.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.VisualPlanConfirmDTO;
import com.ecommerce.detail.ai.dto.ImageJobCreateDTO;
import com.ecommerce.detail.ai.dto.VisualPlanBatchStatusDTO;
import java.util.List;
import java.util.Map;
import com.ecommerce.detail.ai.dto.VisualPlanDTO;
import com.ecommerce.detail.ai.entity.VisualPlan;

public interface VisualPlanService extends IService<VisualPlan> {

    PageResult<VisualPlanDTO> listVisualPlans(int pageNum, int pageSize, Long productDetailId, String status);

    Long createVisualPlan(VisualPlanDTO dto);

    VisualPlanDTO getVisualPlanById(Long id);

    boolean updateVisualPlan(Long id, VisualPlanDTO dto);

    VisualPlanDTO confirmVisualPlan(Long id, VisualPlanConfirmDTO dto);
    List<Long> dispatchFromVisualPlan(Long id, List<ImageJobCreateDTO> jobs);
    VisualPlanBatchStatusDTO getBatchStatus(Long id);

    /** Retry all FAILED or CANCELED image jobs in a visual plan batch. Returns the count of jobs retried. */
    int retryAllFailedJobs(Long planId);

    /** Cancel all PENDING or RUNNING image jobs in a visual plan batch. Returns the count of jobs canceled. */
    int cancelBatch(Long planId);

    /** Get batch results grouped by slot, optionally filtered to a single slot. */
    Map<String, Object> getBatchResultsBySlot(Long planId, String slotFilter);
}
