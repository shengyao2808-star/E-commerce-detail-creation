package com.ecommerce.detail.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.AuditDTO;
import com.ecommerce.detail.ai.entity.AuditRecord;

/**
 * 审核服务接口
 * 
 * @author Administrator
 * @version 1.0.0
 */
public interface AuditService extends IService<AuditRecord> {

    /**
     * 提交审核
     * 
     * @param dto 审核DTO
     * @return 审核记录ID
     */
    Long submitAudit(AuditDTO dto);

    /**
     * 批量提交审核
     * 
     * @param dtos 审核列表
     * @return 成功提交数量
     */
    int batchSubmitAudit(java.util.List<AuditDTO> dtos);

    /**
     * 根据商品详情页ID获取审核记录
     *
     * @param productDetailId 商品详情页ID
     * @return 审核记录
     */
    AuditRecord getAuditByProductDetailId(Long productDetailId);

    /**
     * 根据审核记录ID获取审核记录
     *
     * @param id 审核记录ID
     * @return 审核记录
     */
    AuditRecord getAuditById(Long id);

    /**
     * 分页查询审核记录
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param status 审核状态
     * @param auditor 审核人
     * @return 分页结果
     */
    PageResult<AuditRecord> listAuditRecords(int pageNum, int pageSize, Integer status, String auditor);

    /**
     * 审核通过
     * 
     * @param id 审核记录ID
     * @param comment 审核意见
     * @return 是否成功
     */
    boolean approveAudit(Long id, String comment);

    /**
     * 审核拒绝
     * 
     * @param id 审核记录ID
     * @param comment 拒绝原因
     * @return 是否成功
     */
    boolean rejectAudit(Long id, String comment);

    /**
     * 退回审核
     *
     * @param id 审核记录ID
     * @param comment 退回原因
     * @return 是否成功
     */
    boolean returnAudit(Long id, String comment);

    /**
     * 撤回审核
     * 
     * @param id 审核记录ID
     * @return 是否成功
     */
    boolean withdrawAudit(Long id);

    /**
     * 重新审核
     * 
     * @param id 审核记录ID
     * @return 是否成功
     */
    boolean reaudit(Long id);
}
