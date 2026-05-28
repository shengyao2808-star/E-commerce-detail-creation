package com.ecommerce.detail.ai.service;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.CostConfigDTO;

public interface CostConfigService {

    PageResult<CostConfigDTO> listCostConfigs(int pageNum, int pageSize, String providerType, String providerCode);

    Long createCostConfig(CostConfigDTO dto);

    CostConfigDTO getCostConfigById(Long id);

    boolean updateCostConfig(Long id, CostConfigDTO dto);

    boolean deleteCostConfig(Long id);

    CostConfigDTO findByProvider(String providerType, String providerCode);
}
