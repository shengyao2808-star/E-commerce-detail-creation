package com.ecommerce.detail.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.DesignDraftDTO;
import com.ecommerce.detail.ai.entity.DesignDraft;

public interface DesignDraftService extends IService<DesignDraft> {

    Long createDesignDraft(DesignDraftDTO dto);

    DesignDraftDTO getDesignDraftById(Long id);

    PageResult<DesignDraftDTO> listDesignDrafts(int pageNum, int pageSize, Long productDetailId, Long productMaterialId, String status);

    boolean updateDesignDraft(Long id, DesignDraftDTO dto);
}
