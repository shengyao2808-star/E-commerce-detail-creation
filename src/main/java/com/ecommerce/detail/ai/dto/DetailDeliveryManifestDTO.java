package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * P3.12: Delivery manifest with full traceability.
 * Each delivery file can be traced back to: task -> result -> tool.
 */
@Data
public class DetailDeliveryManifestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long detailCompositionId;
    private Long productDetailId;
    private Boolean deliverable;
    private String compositionStatus;
    private String outputPath;
    private String outputFileName;
    private Long outputFileSize;
    private Integer outputWidth;
    private Integer outputHeight;
    private String latestQualityCheckStatus;
    private Integer latestQualityCheckIssueCount;
    private String latestQualityCheckScreenshotPath;
    private LocalDateTime latestQualityCheckTime;

    /** Generation results linked to this product detail, with full lineage */
    private List<Map<String, Object>> generationResults;

    /** Ordered list of tool codes used in the delivery chain */
    private List<String> toolchain;

    private LocalDateTime generatedAt;

    // --- P3.12 traceability fields ---

    /** The confirmed visual plan ID that originated this delivery chain */
    private Long visualPlanId;

    /** Per-delivery-file source trace: each entry maps file -> {taskType, taskId, resultId, toolCode} */
    private List<Map<String, Object>> deliveryFileSources;

    /** Image job IDs that contributed generation results */
    private List<Long> imageJobIds;

    /** Map of imageJobId -> toolCode for the generation jobs */
    private Map<String, String> imageJobToolCodes;

    /** Whether all referenced generation result URLs are accessible */
    private Boolean allResultUrlsAccessible;

    /** Whether there are duplicate images in the generation results */
    private Boolean hasDuplicateImages;

    /** QA dimension/ratio validation summary */
    private Map<String, Object> dimensionValidation;
}