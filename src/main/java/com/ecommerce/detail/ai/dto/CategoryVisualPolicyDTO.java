package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class CategoryVisualPolicyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String categoryCode;

    private String categoryName;

    private String modelPolicy;

    private String modelConsistencyLevel;

    private List<String> allowedShotTypes;

    private Map<String, Object> requiredMainImages;

    private Map<String, Object> detailScreenCountRange;

    private List<String> riskRules;

    private String status;

    private Integer version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
