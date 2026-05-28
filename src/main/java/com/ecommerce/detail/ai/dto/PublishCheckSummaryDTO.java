package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.util.List;

@Data
public class PublishCheckSummaryDTO {

    private Long productDetailId;

    private boolean publishable;

    private int totalChecks;

    private int passedChecks;

    private int failedChecks;

    private int hardFailedChecks;

    private int softFailedChecks;

    private int overriddenChecks;

    private List<PublishCheckDTO> items;
}
