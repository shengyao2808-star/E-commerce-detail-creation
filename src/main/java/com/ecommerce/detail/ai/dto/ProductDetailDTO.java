package com.ecommerce.detail.ai.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 商品详情页传输对象
 * 
 * @author Administrator
 * @version 1.0.0
 */
@Data
public class ProductDetailDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品资料ID
     */
    @NotNull(message = "商品资料ID不能为空")
    private Long materialId;

    /**
     * 品牌ID
     */
    @NotNull(message = "品牌ID不能为空")
    private Long brandId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 分类
     */
    private String category;

    /**
     * SKU
     */
    private String sku;

    /**
     * 价格
     */
    private java.math.BigDecimal price;

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 副标题
     */
    private String subtitle;

    /**
     * 核心卖点列表
     */
    private List<String> sellingPoints;

    /**
     * SEO关键词列表
     */
    private List<String> seoKeywords;

    /**
     * 图片模板ID
     */
    private Long imageTemplateId;

    /**
     * AI生成的完整内容（JSON格式，包含标题、副标题、卖点、详细描述等）
     */
    private String aiGeneratedContent;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 图片列表
     */
    private List<String> images;

    /**
     * 视频列表
     */
    private List<String> videos;

    /**
     * 文档列表
     */
    private List<String> documents;

    /**
     * 生成人
     */
    private String creator;
}
