package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PostProcessTaskListQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer pageNum;
    private Integer pageSize;
    private Long sourceGenerationResultId;
    private String toolCode;
    private String operation;
    private String status;
}
