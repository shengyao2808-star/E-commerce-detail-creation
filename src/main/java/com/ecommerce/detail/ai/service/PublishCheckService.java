package com.ecommerce.detail.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.detail.ai.dto.PublishCheckDTO;
import com.ecommerce.detail.ai.dto.PublishCheckOverrideRequestDTO;
import com.ecommerce.detail.ai.dto.PublishCheckSummaryDTO;
import com.ecommerce.detail.ai.entity.PublishCheck;

import java.util.List;

public interface PublishCheckService extends IService<PublishCheck> {

    /**
     * Run all publish checks for a product detail and persist results.
     * Returns the summary with all check items.
     */
    PublishCheckSummaryDTO runChecks(Long productDetailId);

    /**
     * List all persisted check items for a product detail.
     */
    List<PublishCheckDTO> listChecks(Long productDetailId);

    /**
     * Override a blocking check item. Records reason and operator.
     */
    boolean overrideCheck(Long checkId, PublishCheckOverrideRequestDTO dto);

    /**
     * Get the publish-check summary: publishable = all HARD checks PASS or overridden.
     */
    PublishCheckSummaryDTO getSummary(Long productDetailId);
}
