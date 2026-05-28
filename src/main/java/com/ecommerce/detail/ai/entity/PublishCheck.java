package com.ecommerce.detail.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("publish_check")
public class PublishCheck implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productDetailId;

    private String checkType;

    private String targetType;

    private String targetId;

    private String targetField;

    private String severity;

    private String status;

    private String message;

    private String detailsJson;

    private Integer overridden;

    private String overrideReason;

    private String overrideOperator;

    private LocalDateTime overrideTime;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
