package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class SkcPolicyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String policyName;

    private String categoryCode;

    private Integer colorCount;

    private Integer specCount;

    private List<Map<String, Object>> colors;

    private List<Map<String, Object>> specs;

    private String renderMode;

    private String variantDisplayMode;

    private List<String> generationRules;

    private String status;

    private Integer version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
