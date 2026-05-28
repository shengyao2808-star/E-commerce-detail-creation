package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class ImageJobCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String taskName;

    private String toolCode;

    private Map<String, Object> inputData;

    private Long visualPlanId;

    private String slot;

    private String ratio;

    private Integer promptVersion;

    private Long modelProfileId;

    private String sourceSnapshotJson;
}
