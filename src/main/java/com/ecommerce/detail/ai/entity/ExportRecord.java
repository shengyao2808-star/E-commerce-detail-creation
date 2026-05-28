package com.ecommerce.detail.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Export record entity with P3.12 manifest linkage.
 */
@Data
@TableName("export_record")
public class ExportRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** Product detail ID */
    private Long productDetailId;

    /** Export format (WORD/MARKDOWN/JSON/HTML/TXT) */
    private String exportFormat;

    /** Exported file path */
    private String filePath;

    /** File name */
    private String fileName;

    /** File size in bytes */
    private Long fileSize;

    /** Export status (0=pending, 1=success, 2=failed) */
    private Integer exportStatus;

    /** Error message */
    private String errorMessage;

    /** Exporter */
    private String exporter;

    /** Export time */
    private LocalDateTime exportTime;

    /** P3.12: linked detail composition ID */
    private Long detailCompositionId;

    /** P3.12: delivery manifest snapshot at export time */
    private String manifestJson;

    /** P3.12: manifest consistency check result */
    private Boolean manifestConsistent;

    /** P3.12: linked QA check ID */
    private Long qaCheckId;

    /** P3.12: QA status at export time */
    private String qaStatus;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}