package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ImageJobDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String taskName;

    private String toolCode;

    private Map<String, Object> inputData;

    private String status;

    private Integer progress;

    private String externalJobId;

    private String errorMessage;

    private Long visualPlanId;

    private String slot;

    private String ratio;

    private Integer promptVersion;

    private Long modelProfileId;

    private String sourceSnapshotJson;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
