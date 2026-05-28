package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class GenerationResultListQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer pageNum = 1;

    private Integer pageSize = 20;

    private Long imageJobId;

    private Boolean selected;

    private String complianceStatus;
}
