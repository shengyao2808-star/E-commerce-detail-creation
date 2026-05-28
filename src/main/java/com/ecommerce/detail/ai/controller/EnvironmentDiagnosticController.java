package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.EnvironmentDiagnosticDTO;
import com.ecommerce.detail.ai.service.EnvironmentDiagnosticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system")
public class EnvironmentDiagnosticController {

    @Autowired
    private EnvironmentDiagnosticService diagnosticService;

    /**
     * Returns a full environment diagnostic report.
     *
     * Each tool adapter is classified as:
     * - AVAILABLE     : enabled=true and base-url is set
     * - NOT_CONFIGURED: enabled=false (default for all tools)
     * - CONFIG_ERROR  : enabled=true but base-url is missing
     *
     * The AI relay follows the same classification.
     * File path allowlists are validated for existence and writability.
     * Export format implementation status is reported.
     *
     * The top-level overallStatus is:
     * - READY     : at least one relay or tool is AVAILABLE, no CONFIG_ERROR
     * - DEGRADED  : at least one CONFIG_ERROR exists
     * - NOT_READY : nothing is configured
     */
    @GetMapping("/diagnostics")
    public Result<EnvironmentDiagnosticDTO> getDiagnostics() {
        return Result.success(diagnosticService.diagnose());
    }
}
