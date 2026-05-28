package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DetailCompositionListQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer pageNum;
    private Integer pageSize;
    private Long productDetailId;
    private String status;
    private String toolCode;
    private String keyword;
}
