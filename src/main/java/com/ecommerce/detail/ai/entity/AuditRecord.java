package com.ecommerce.detail.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审核记录实体类
 * 
 * @author Administrator
 * @version 1.0.0
 */
@Data
@TableName("audit_record")
public class AuditRecord implements Serializable {

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
     * 审核类型（AUTO-自动审核，MANUAL-人工审核）
     */
    private String auditType;

    /**
     * 审核状态（0-待审核，1-审核中，2-通过，3-拒绝，4-需修改）
     */
    private Integer auditStatus;

    /**
     * 风险等级（1-低，2-中，3-高，4-极高）
     */
    private Integer riskLevel;

    /**
     * 风险项列表（JSON数组）
     */
    private String riskItems;

    /**
     * 审核意见
     */
    private String auditComment;

    /**
     * 修改建议（JSON对象）
     */
    private String modificationSuggestions;

    /**
     * 提交人
     */
    private String submitter;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 审核人
     */
    private String auditor;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核耗时（秒）
     */
    private Integer auditDuration;

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
