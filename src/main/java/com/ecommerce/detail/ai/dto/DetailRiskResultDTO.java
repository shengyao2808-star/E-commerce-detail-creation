package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class DetailRiskResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long productDetailId;
    private String riskLevel;
    private String riskDescription;
    private boolean hasRisk;
    private List<String> issues;
    private Map<String, List<String>> issueDetails;
    private List<String> suggestions;
    private String content;
    private Integer auditStatus;
    private String auditComment;
    private LocalDateTime updateTime;
}
