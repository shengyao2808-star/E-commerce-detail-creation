package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class GenerationResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long imageJobId;

    private String resultUrl;

    private String thumbnailUrl;

    private String prompt;

    private Map<String, Object> params;

    private String complianceStatus;

    private Boolean selected;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
