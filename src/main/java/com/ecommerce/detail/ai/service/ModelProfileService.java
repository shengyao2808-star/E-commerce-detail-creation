package com.ecommerce.detail.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.ModelProfileDTO;
import com.ecommerce.detail.ai.entity.ModelProfile;

public interface ModelProfileService extends IService<ModelProfile> {

    PageResult<ModelProfileDTO> listModelProfiles(int pageNum, int pageSize, String keyword, String status);

    Long createModelProfile(ModelProfileDTO dto);

    ModelProfileDTO getModelProfileById(Long id);

    boolean updateModelProfile(Long id, ModelProfileDTO dto);

    ModelProfileDTO confirmModelProfile(Long id);
}