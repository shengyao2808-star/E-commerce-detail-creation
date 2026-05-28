package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PromptTemplateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String templateName;
    private String category;
    private String sceneType;
    private String platform;
    private String style;
    private String positivePrompt;
    private String negativePrompt;
    private List<String> styleTags;
    private List<String> constraints;
    private String description;
    private String previewImageUrl;
    private Integer usageCount;
    private BigDecimal rating;
    private String source;
    private String sourceRef;
    private String language;
    private String author;
    private List<String> tags;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}