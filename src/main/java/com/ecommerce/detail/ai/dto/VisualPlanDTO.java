package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class VisualPlanDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long productDetailId;

    private String planName;

    private String categoryCode;

    private Long categoryVisualPolicyId;

    private Long modelProfileId;

    private Long skcPolicyId;

    private List<Long> promptWorkbenchEntryIds;

    private Map<String, Object> inputData;

    private Map<String, Object> planData;

    private Map<String, Object> snapshotData;

    private String status;

    private Integer version;

    private LocalDateTime confirmedTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
