package com.ecommerce.detail.ai.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 商品资料传输对象
 * 
 * @author Administrator
 * @version 1.0.0
 */
@Data
public class ProductMaterialDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 品牌ID
     */
    @NotNull(message = "品牌ID不能为空")
    private Long brandId;

    /**
     * 品牌名称
     */
    @NotBlank(message = "品牌名称不能为空")
    private String brandName;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    private String productName;

    /**
     * 商品SKU
     */
    private String productSku;

    /**
     * 商品类目
     */
    private String category;

    /**
     * 上传人
     */
    private String uploader;

    /**
     * SKU
     */
    private String sku;

    /**
     * 价格
     */
    private java.math.BigDecimal price;

    /**
     * 描述
     */
    private String description;

    /**
     * 图片列表
     */
    private java.util.List<String> images;

    /**
     * 视频列表
     */
    private java.util.List<String> videos;

    /**
     * 文档列表
     */
    private java.util.List<String> documents;
}
