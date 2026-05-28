package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.PublishCheckDTO;
import com.ecommerce.detail.ai.dto.PublishCheckOverrideRequestDTO;
import com.ecommerce.detail.ai.dto.PublishCheckSummaryDTO;
import com.ecommerce.detail.ai.service.PublishCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/publish-checks")
public class PublishCheckController {

    @Autowired
    private PublishCheckService publishCheckService;

    @PostMapping("/run/{productDetailId}")
    public Result<PublishCheckSummaryDTO> runChecks(@PathVariable Long productDetailId) {
        return Result.success(publishCheckService.runChecks(productDetailId));
    }

    @GetMapping("/list/{productDetailId}")
    public Result<List<PublishCheckDTO>> listChecks(@PathVariable Long productDetailId) {
        return Result.success(publishCheckService.listChecks(productDetailId));
    }

    @GetMapping("/summary/{productDetailId}")
    public Result<PublishCheckSummaryDTO> getSummary(@PathVariable Long productDetailId) {
        return Result.success(publishCheckService.getSummary(productDetailId));
    }

    @PostMapping("/{checkId}/override")
    public Result<Boolean> overrideCheck(
            @PathVariable Long checkId,
            @RequestBody PublishCheckOverrideRequestDTO dto) {
        return Result.success(publishCheckService.overrideCheck(checkId, dto));
    }
}
