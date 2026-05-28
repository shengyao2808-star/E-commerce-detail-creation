package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ModelProfileDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String displayName;

    private String frontImage;

    private String sideImage;

    private String backImage;

    private BigDecimal height;

    private BigDecimal weight;

    private BigDecimal bust;

    private BigDecimal waist;

    private BigDecimal hip;

    private List<String> styleTags;

    private List<String> categoryScopes;

    private String authorizationStatus;

    private String status;

    private Integer version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
