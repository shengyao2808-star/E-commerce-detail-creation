package com.ecommerce.detail.ai.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class DetailCompositionCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "productDetailId must not be null")
    private Long productDetailId;

    private String taskName;

    private String toolCode;

    private ProductDetailDTO detailData;

    private List<String> moduleOrder;
}
