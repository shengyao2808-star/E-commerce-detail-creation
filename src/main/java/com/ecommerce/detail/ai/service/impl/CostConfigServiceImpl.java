package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.CostConfigDTO;
import com.ecommerce.detail.ai.entity.CostConfig;
import com.ecommerce.detail.ai.mapper.CostConfigMapper;
import com.ecommerce.detail.ai.service.CostConfigService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CostConfigServiceImpl extends ServiceImpl<CostConfigMapper, CostConfig> implements CostConfigService {

    @Override
    public PageResult<CostConfigDTO> listCostConfigs(int pageNum, int pageSize, String providerType, String providerCode) {
        pageNum = Math.max(pageNum, 1);
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        LambdaQueryWrapper<CostConfig> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(providerType)) {
            wrapper.eq(CostConfig::getProviderType, providerType.trim());
        }
        if (StringUtils.hasText(providerCode)) {
            wrapper.like(CostConfig::getProviderCode, providerCode.trim());
        }
        wrapper.orderByDesc(CostConfig::getCreateTime);

        Page<CostConfig> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<CostConfigDTO> records = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.success(records, pageNum, pageSize, page.getTotal());
    }

    @Override
    public Long createCostConfig(CostConfigDTO dto) {
        CostConfig entity = new EntityBuilder()
                .providerType(requireText(dto.getProviderType(), "providerType"))
                .providerCode(requireText(dto.getProviderCode(), "providerCode"))
                .unitPrice(dto.getUnitPrice())
                .unitType(dto.getUnitType() != null ? dto.getUnitType() : "PER_CALL")
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "USD")
                .description(dto.getDescription())
                .build();
        this.save(entity);
        return entity.getId();
    }

    @Override
    public CostConfigDTO getCostConfigById(Long id) {
        CostConfig entity = this.getById(id);
        if (entity == null) {
            return null;
        }
        return toDTO(entity);
    }

    @Override
    public boolean updateCostConfig(Long id, CostConfigDTO dto) {
        CostConfig entity = this.getById(id);
        if (entity == null) {
            return false;
        }
        if (dto.getProviderType() != null) entity.setProviderType(dto.getProviderType());
        if (dto.getProviderCode() != null) entity.setProviderCode(dto.getProviderCode());
        if (dto.getUnitPrice() != null) entity.setUnitPrice(dto.getUnitPrice());
        if (dto.getUnitType() != null) entity.setUnitType(dto.getUnitType());
        if (dto.getCurrency() != null) entity.setCurrency(dto.getCurrency());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        return this.updateById(entity);
    }

    @Override
    public boolean deleteCostConfig(Long id) {
        return this.removeById(id);
    }

    @Override
    public CostConfigDTO findByProvider(String providerType, String providerCode) {
        LambdaQueryWrapper<CostConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CostConfig::getProviderType, providerType)
               .eq(CostConfig::getProviderCode, providerCode);
        CostConfig entity = this.getOne(wrapper, false);
        return entity != null ? toDTO(entity) : null;
    }

    private CostConfigDTO toDTO(CostConfig entity) {
        CostConfigDTO dto = new CostConfigDTO();
        dto.setId(entity.getId());
        dto.setProviderType(entity.getProviderType());
        dto.setProviderCode(entity.getProviderCode());
        dto.setUnitPrice(entity.getUnitPrice());
        dto.setUnitType(entity.getUnitType());
        dto.setCurrency(entity.getCurrency());
        dto.setDescription(entity.getDescription());
        return dto;
    }

    private String requireText(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static class EntityBuilder {
        private final CostConfig entity = new CostConfig();

        EntityBuilder providerType(String v) { entity.setProviderType(v); return this; }
        EntityBuilder providerCode(String v) { entity.setProviderCode(v); return this; }
        EntityBuilder unitPrice(java.math.BigDecimal v) { entity.setUnitPrice(v); return this; }
        EntityBuilder unitType(String v) { entity.setUnitType(v); return this; }
        EntityBuilder currency(String v) { entity.setCurrency(v); return this; }
        EntityBuilder description(String v) { entity.setDescription(v); return this; }
        CostConfig build() { return entity; }
    }
}
