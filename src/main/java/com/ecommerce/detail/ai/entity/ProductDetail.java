package com.ecommerce.detail.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品详情页实体类
 * 
 * @author Administrator
 * @version 1.0.0
 */
@Data
@TableName("product_detail")
public class ProductDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品资料ID
     */
    private Long materialId;

    private String productName;

    /**
     * 品牌ID
     */
    private Long brandId;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 标题
     */
    private String title;

    /**
     * 副标题
     */
    private String subtitle;

    /**
     * 核心卖点列表（JSON数组）
     */
    private String sellingPoints;

    /**
     * SEO关键词列表（JSON数组）
     */
    private String seoKeywords;

    /**
     * 模块顺序（JSON数组）
     */
    @TableField("module_order")
    private String moduleOrder;

    /**
     * 图片模板ID
     */
    private Long imageTemplateId;

    /**
     * 商品SKU
     */
    private String sku;

    /**
     * 分类
     */
    private String category;

    /**
     * 价格
     */
    private java.math.BigDecimal price;

    /**
     * 描述
     */
    private String description;

    /**
     * AI生成的完整内容（JSON格式，包含标题、副标题、卖点、详细描述等）
     */
    private String aiGeneratedContent;

    /**
     * 图片列表（JSON数组）
     */
    private String images;

    /**
     * 视频列表（JSON数组）
     */
    private String videos;

    /**
     * 文档列表（JSON数组）
     */
    private String documents;

    /**
     * 风险等级（低、中、高）
     */
    private String riskLevel;

    /**
     * 风险描述
     */
    private String riskDescription;

    /**
     * 审核状态（0-待审核，1-审核中，2-通过，3-拒绝，4-需修改）
     */
    private Integer auditStatus;

    /**
     * 审核人
     */
    private String auditor;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核意见
     */
    private String auditComment;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 是否当前版本
     */
    private Boolean isCurrentVersion;

    /**
     * 逻辑删除标识
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updater;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
