package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.enums.AuditStatus;
import com.ecommerce.detail.ai.dto.AuditDTO;
import com.ecommerce.detail.ai.entity.AuditRecord;
import com.ecommerce.detail.ai.mapper.AuditRecordMapper;
import com.ecommerce.detail.ai.service.AuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审核服务实现类
 * 
 * @author Administrator
 * @version 1.0.0
 */
@Slf4j
@Service
public class AuditServiceImpl extends ServiceImpl<AuditRecordMapper, AuditRecord> implements AuditService {

    @Override
    public Long submitAudit(AuditDTO dto) {
        // 参数校验
        if (dto == null || dto.getProductDetailId() == null) {
            throw new IllegalArgumentException("商品详情页ID不能为空");
        }

        // 检查是否已有待审核记录
        AuditRecord existing = this.lambdaQuery()
                .eq(AuditRecord::getProductDetailId, dto.getProductDetailId())
                .eq(AuditRecord::getAuditStatus, AuditStatus.PENDING.getCode())
                .one();
        
        if (existing != null) {
            throw new RuntimeException("该商品详情页已有待审核记录，请先处理现有审核");
        }

        AuditRecord record = new AuditRecord();
        record.setProductDetailId(dto.getProductDetailId());
        record.setAuditor(dto.getAuditor());
        record.setAuditTime(LocalDateTime.now());
        record.setAuditStatus(AuditStatus.PENDING.getCode());
        record.setAuditComment(dto.getAuditComment());
        record.setRiskLevel(dto.getRiskLevel());
        record.setSubmitter(dto.getSubmitter());
        record.setSubmitTime(LocalDateTime.now());
        
        this.save(record);
        log.info("提交审核成功，ID: {}, 商品详情页ID: {}", record.getId(), dto.getProductDetailId());
        return record.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSubmitAudit(List<AuditDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (AuditDTO dto : dtos) {
            try {
                submitAudit(dto);
                successCount++;
            } catch (Exception e) {
                log.error("批量提交审核失败，商品详情页ID: {}", dto.getProductDetailId(), e);
            }
        }
        
        log.info("批量提交审核完成，总数: {}, 成功: {}", dtos.size(), successCount);
        return successCount;
    }

    @Override
    public AuditRecord getAuditByProductDetailId(Long productDetailId) {
        if (productDetailId == null) {
            throw new IllegalArgumentException("商品详情页ID不能为空");
        }
        
        return this.lambdaQuery()
                .eq(AuditRecord::getProductDetailId, productDetailId)
                .orderByDesc(AuditRecord::getAuditTime)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public AuditRecord getAuditById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("审核记录ID不能为空");
        }

        AuditRecord record = this.getById(id);
        if (record == null) {
            throw new RuntimeException("审核记录不存在，ID: " + id);
        }
        return record;
    }

    @Override
    public PageResult<AuditRecord> listAuditRecords(int pageNum, int pageSize, Integer status, String auditor) {
        // 参数校验和默认值设置
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        Page<AuditRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AuditRecord> wrapper = new LambdaQueryWrapper<>();
        
        // 状态筛选
        if (status != null) {
            wrapper.eq(AuditRecord::getAuditStatus, status);
        }
        
        // 审核人筛选
        if (auditor != null && !auditor.trim().isEmpty()) {
            wrapper.like(AuditRecord::getAuditor, auditor);
        }
        
        // 按审核时间倒序排列
        wrapper.orderByDesc(AuditRecord::getAuditTime);
        
        Page<AuditRecord> resultPage = this.page(page, wrapper);
        
        return new PageResult<>(resultPage.getTotal(), resultPage.getRecords(), pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveAudit(Long id, String comment) {
        if (id == null) {
            throw new IllegalArgumentException("审核记录ID不能为空");
        }

        AuditRecord record = this.getById(id);
        if (record == null) {
            throw new RuntimeException("审核记录不存在，ID: " + id);
        }

        if (!AuditStatus.PENDING.getCode().equals(record.getAuditStatus())) {
            throw new RuntimeException("该审核记录状态不是待审核，无法通过");
        }

        // 更新审核状态为通过
        record.setAuditStatus(AuditStatus.APPROVED.getCode());
        record.setAuditComment(comment);
        record.setAuditTime(LocalDateTime.now());
        
        boolean result = this.updateById(record);
        log.info("审核通过{}，ID: {}", result ? "成功" : "失败", id);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectAudit(Long id, String comment) {
        if (id == null) {
            throw new IllegalArgumentException("审核记录ID不能为空");
        }

        AuditRecord record = this.getById(id);
        if (record == null) {
            throw new RuntimeException("审核记录不存在，ID: " + id);
        }

        if (!AuditStatus.PENDING.getCode().equals(record.getAuditStatus())) {
            throw new RuntimeException("该审核记录状态不是待审核，无法拒绝");
        }

        // 更新审核状态为拒绝
        record.setAuditStatus(AuditStatus.REJECTED.getCode());
        record.setAuditComment(comment);
        record.setAuditTime(LocalDateTime.now());
        
        boolean result = this.updateById(record);
        log.info("审核拒绝{}，ID: {}", result ? "成功" : "失败", id);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnAudit(Long id, String comment) {
        if (id == null) {
            throw new IllegalArgumentException("审核记录ID不能为空");
        }

        AuditRecord record = this.getById(id);
        if (record == null) {
            throw new RuntimeException("审核记录不存在，ID: " + id);
        }

        Integer status = record.getAuditStatus();
        if (!AuditStatus.PENDING.getCode().equals(status) && !AuditStatus.AUDITING.getCode().equals(status)) {
            throw new RuntimeException("只有待审核或审核中状态的记录才能退回修改");
        }

        record.setAuditStatus(AuditStatus.NEED_MODIFY.getCode());
        record.setAuditComment(comment);
        record.setAuditTime(LocalDateTime.now());

        boolean result = this.updateById(record);
        log.info("退回修改{}，ID: {}", result ? "成功" : "失败", id);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean withdrawAudit(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("审核记录ID不能为空");
        }

        AuditRecord record = this.getById(id);
        if (record == null) {
            throw new RuntimeException("审核记录不存在，ID: " + id);
        }

        if (!AuditStatus.PENDING.getCode().equals(record.getAuditStatus())) {
            throw new RuntimeException("只有待审核状态的记录才能撤回");
        }

        // 删除审核记录
        boolean result = this.removeById(id);
        log.info("撤回审核{}，ID: {}", result ? "成功" : "失败", id);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reaudit(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("审核记录ID不能为空");
        }

        AuditRecord record = this.getById(id);
        if (record == null) {
            throw new RuntimeException("审核记录不存在，ID: " + id);
        }

        if (AuditStatus.PENDING.getCode().equals(record.getAuditStatus())) {
            throw new RuntimeException("该审核记录已经是待审核状态");
        }

        // 重置为待审核状态
        record.setAuditStatus(AuditStatus.PENDING.getCode());
        record.setAuditComment(null);
        record.setAuditTime(null);
        
        boolean result = this.updateById(record);
        log.info("重新审核{}，ID: {}", result ? "成功" : "失败", id);
        return result;
    }
}
