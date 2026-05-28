package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ResearchTaskDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String taskName;

    private String category;

    private String owner;

    private String status;

    private Map<String, Object> inputData;

    private Map<String, Object> resultData;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
