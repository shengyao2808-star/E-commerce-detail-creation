package com.ecommerce.detail.ai.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("prompt_template")
public class PromptTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String templateName;

    private String category;

    private String sceneType;

    private String platform;

    private String style;

    private String positivePrompt;

    private String negativePrompt;

    private String styleTagsJson;

    private String constraintsJson;

    private String description;

    private String previewImageUrl;

    private Integer usageCount;

    private BigDecimal rating;

    private String source;

    private String sourceRef;

    private String language;

    private String author;

    private String tagsJson;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}