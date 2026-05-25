package com.ecommerce.detail.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 品牌模板实体类
 * 
 * @author Administrator
 * @version 1.0.0
 */
@Data
@TableName("brand_template")
public class BrandTemplate implements Serializable {

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
     * 模板名称
     */
    private String templateName;

    /**
     * 模板类型（STANDARD/PROMOTION/SEASONAL）
     */
    private String templateType;

    /**
     * 模板内容（JSON格式，包含标题、副标题、模块结构等）
     */
    private String templateContent;

    /**
     * 风格标签（逗号分隔）
     */
    private String styleTags;

    /**
     * 风格描述
     */
    private String styleDescription;

    /**
     * 适用类目（逗号分隔）
     */
    private String applicableCategories;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 使用次数
     */
    private Integer usageCount;

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
