package com.ecommerce.detail.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.PromptWorkbenchEntryDTO;
import com.ecommerce.detail.ai.dto.PromptWorkbenchRequestDTO;
import com.ecommerce.detail.ai.entity.PromptWorkbenchEntry;

public interface PromptWorkbenchService extends IService<PromptWorkbenchEntry> {

    PageResult<PromptWorkbenchEntryDTO> listPromptWorkbenchEntries(int pageNum, int pageSize, String entryType, String status);

    PromptWorkbenchEntryDTO getPromptWorkbenchEntryById(Long id);

    PromptWorkbenchEntryDTO createGuidedPrompt(PromptWorkbenchRequestDTO dto);

    PromptWorkbenchEntryDTO expandPrompt(PromptWorkbenchRequestDTO dto);

    PromptWorkbenchEntryDTO imageToPrompt(PromptWorkbenchRequestDTO dto);
}
