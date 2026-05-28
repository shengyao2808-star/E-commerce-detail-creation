package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class PromptWorkbenchEntryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String entryType;

    private String taskName;

    private String toolCode;

    private String status;

    private Integer version;

    private Map<String, Object> inputData;

    private Map<String, Object> outputData;

    private String outputText;

    private String errorMessage;

    private String positivePrompt;

    private String negativePrompt;

    private String shotScript;

    private String composition;

    private String lighting;

    private String camera;

    private List<String> styleTags;

    private Map<String, Object> sourceData;

    private List<String> riskWarnings;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
