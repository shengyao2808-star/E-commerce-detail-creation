package com.ecommerce.detail.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.detail.ai.entity.AuditRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审核记录Mapper接口
 * 
 * @author Administrator
 * @version 1.0.0
 */
@Mapper
public interface AuditRecordMapper extends BaseMapper<AuditRecord> {
}
