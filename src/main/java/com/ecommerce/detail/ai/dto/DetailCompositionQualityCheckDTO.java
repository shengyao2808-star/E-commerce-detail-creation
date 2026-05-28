package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DetailCompositionQualityCheckDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long detailCompositionId;

    private String toolCode;

    private String status;

    private Integer issueCount;

    private List<String> issues;

    private String screenshotPath;

    private String errorMessage;

    private LocalDateTime checkTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
