package com.ecommerce.detail.ai.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 审核传输对象
 * 
 * @author Administrator
 * @version 1.0.0
 */
@Data
public class AuditDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品详情页ID
     */
    @NotNull(message = "商品详情页ID不能为空")
    private Long productDetailId;

    /**
     * 审核状态（2-通过，3-拒绝，4-需修改）
     */
    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;

    /**
     * 审核意见
     */
    @NotBlank(message = "审核意见不能为空")
    private String auditComment;

    /**
     * 审核人
     */
    @NotBlank(message = "审核人不能为空")
    private String auditor;

    /**
     * 风险等级（1-低，2-中，3-高）
     */
    private Integer riskLevel;

    /**
     * 提交人
     */
    private String submitter;
}
