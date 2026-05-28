package com.ecommerce.detail.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.PromptTemplateCreateDTO;
import com.ecommerce.detail.ai.dto.PromptTemplateDTO;
import com.ecommerce.detail.ai.entity.PromptTemplate;

public interface PromptTemplateService extends IService<PromptTemplate> {

    PageResult<PromptTemplateDTO> listTemplates(int pageNum, int pageSize, String category,
                                                 String platform, String style, String source, String keyword);

    PromptTemplateDTO getTemplateById(Long id);

    PromptTemplateDTO createTemplate(PromptTemplateCreateDTO dto);

    PromptTemplateDTO updateTemplate(Long id, PromptTemplateCreateDTO dto);

    void deleteTemplate(Long id);

    PromptTemplateDTO duplicateTemplate(Long id);

    void incrementUsageCount(Long id);
}