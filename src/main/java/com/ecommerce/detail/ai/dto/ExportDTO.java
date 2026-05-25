package com.ecommerce.detail.ai.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 导出传输对象
 * 
 * @author Administrator
 * @version 1.0.0
 */
@Data
public class ExportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品详情页ID
     */
    @NotNull(message = "商品详情页ID不能为空")
    private Long productDetailId;

    /**
     * 导出格式（WORD/MARKDOWN/JSON/HTML）
     */
    @NotBlank(message = "导出格式不能为空")
    private String exportFormat;

    /**
     * 导出人
     */
    @NotBlank(message = "导出人不能为空")
    private String exporter;
}
