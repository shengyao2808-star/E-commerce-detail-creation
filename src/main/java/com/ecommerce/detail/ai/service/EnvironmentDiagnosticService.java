package com.ecommerce.detail.ai.service;

import com.ecommerce.detail.ai.dto.EnvironmentDiagnosticDTO;

public interface EnvironmentDiagnosticService {

    /**
     * Build a full environment diagnostic report covering
     * AI relay, tool adapters, file path allowlists, and export configuration.
     */
    EnvironmentDiagnosticDTO diagnose();
}
