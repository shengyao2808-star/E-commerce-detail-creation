package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class DetailCompositionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long productDetailId;
    private String taskName;
    private String toolCode;
    private Map<String, Object> inputData;
    private List<String> moduleOrder;
    private String status;
    private Integer progress;
    private String externalJobId;
    private String outputPath;
    private String outputFileName;
    private Long outputFileSize;
    private Integer outputWidth;
    private Integer outputHeight;
    private String mimeType;
    private String latestQualityCheckStatus;
    private Integer latestQualityCheckIssueCount;
    private String latestQualityCheckScreenshotPath;
    private java.time.LocalDateTime latestQualityCheckTime;
    private Boolean deliverable;
    private String errorMessage;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
