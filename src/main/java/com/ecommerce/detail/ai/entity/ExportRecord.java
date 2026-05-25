package com.ecommerce.detail.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 导出记录实体类
 * 
 * @author Administrator
 * @version 1.0.0
 */
@Data
@TableName("export_record")
public class ExportRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品详情页ID
     */
    private Long productDetailId;

    /**
     * 导出格式（WORD/MARKDOWN/JSON/HTML）
     */
    private String exportFormat;

    /**
     * 导出文件路径
     */
    private String filePath;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 导出状态（0-待导出，1-导出中，2-成功，3-失败）
     */
    private Integer exportStatus;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 导出人
     */
    private String exporter;

    /**
     * 导出时间
     */
    private LocalDateTime exportTime;

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
}
