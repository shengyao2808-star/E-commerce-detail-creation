package com.ecommerce.detail.ai.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("post_process_task")
public class PostProcessTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sourceGenerationResultId;

    private String sourceImagePath;

    private String outputImagePath;

    private String toolCode;

    private String operation;

    private String paramsJson;

    private Integer inputWidth;

    private Integer inputHeight;

    private Long inputFileSize;

    private String inputMimeType;

    private Integer outputWidth;

    private Integer outputHeight;

    private Long outputFileSize;

    private String outputMimeType;

    private String sourceChainJson;

    private String status;

    private Integer progress;

    private String errorMessage;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
