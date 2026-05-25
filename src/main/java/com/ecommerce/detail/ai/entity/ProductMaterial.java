package com.ecommerce.detail.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品资料实体类
 * 
 * @author Administrator
 * @version 1.0.0
 */
@Data
@TableName("product_material")
public class ProductMaterial implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 品牌ID
     */
    private Long brandId;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 商品名称
     */
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
     * 商品价格
     */
    private java.math.BigDecimal price;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 图片列表（JSON数组）
     */
    private List<String> images;

    /**
     * 视频列表（JSON数组）
     */
    private List<String> videos;

    /**
     * 文档列表（JSON数组）
     */
    private List<String> documents;

    /**
     * 资料状态（0-草稿，1-已提交，2-审核中，3-审核通过，4-审核拒绝）
     */
    private Integer status;

    /**
     * 原始文件路径
     */
    private String originalFilePath;

    /**
     * 文件类型（WORD/EXCEL/PDF/IMAGE/TEXT）
     */
    private String fileType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 上传人
     */
    private String uploader;

    /**
     * 上传时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime uploadTime;

    /**
     * 解析状态（0-待解析，1-解析中，2-解析成功，3-解析失败）
     */
    private Integer parseStatus;

    /**
     * 解析错误信息
     */
    private String parseError;

    /**
     * 逻辑删除标识
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
