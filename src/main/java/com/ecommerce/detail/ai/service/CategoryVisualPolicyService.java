package com.ecommerce.detail.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.CategoryVisualPolicyDTO;
import com.ecommerce.detail.ai.entity.CategoryVisualPolicy;

public interface CategoryVisualPolicyService extends IService<CategoryVisualPolicy> {

    PageResult<CategoryVisualPolicyDTO> listCategoryVisualPolicies(int pageNum, int pageSize, String keyword, String status);

    Long createCategoryVisualPolicy(CategoryVisualPolicyDTO dto);

    CategoryVisualPolicyDTO getCategoryVisualPolicyById(Long id);

    boolean updateCategoryVisualPolicy(Long id, CategoryVisualPolicyDTO dto);

    CategoryVisualPolicyDTO confirmCategoryVisualPolicy(Long id);
}