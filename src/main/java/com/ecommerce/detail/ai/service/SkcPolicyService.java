package com.ecommerce.detail.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.SkcPolicyDTO;
import com.ecommerce.detail.ai.entity.SkcPolicy;

public interface SkcPolicyService extends IService<SkcPolicy> {

    PageResult<SkcPolicyDTO> listSkcPolicies(int pageNum, int pageSize, String keyword, String status);

    Long createSkcPolicy(SkcPolicyDTO dto);

    SkcPolicyDTO getSkcPolicyById(Long id);

    boolean updateSkcPolicy(Long id, SkcPolicyDTO dto);

    SkcPolicyDTO confirmSkcPolicy(Long id);
}