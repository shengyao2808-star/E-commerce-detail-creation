package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CostConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String providerType;
    private String providerCode;
    private BigDecimal unitPrice;
    private String unitType;
    private String currency;
    private String description;
}
