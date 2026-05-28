package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class ProductContentTaskDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long productDetailId;

    private String taskName;

    private String toolCode;

    private String status;

    private Integer version;

    private Map<String, Object> inputData;

    private Map<String, Object> outputData;

    private String outputText;

    private String title;

    private String subtitle;

    private List<String> sellingPoints;

    private List<Map<String, Object>> detailModules;

    private List<Map<String, Object>> faq;

    private List<String> seoKeywords;

    private List<String> riskWarnings;

    private Map<String, Object> sourceData;

    private List<String> appliedFields;

    private LocalDateTime appliedTime;

    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
