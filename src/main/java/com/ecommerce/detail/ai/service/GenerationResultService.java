package com.ecommerce.detail.ai.service;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.GenerationResultDTO;
import com.ecommerce.detail.ai.dto.GenerationResultListQuery;
import com.ecommerce.detail.ai.dto.GenerationResultSelectionDTO;

public interface GenerationResultService {

    PageResult<GenerationResultDTO> listGenerationResults(GenerationResultListQuery query);

    GenerationResultDTO getGenerationResultById(Long id);

    Long saveGenerationResult(GenerationResultDTO dto);

    Long upsertGenerationResult(GenerationResultDTO dto);

    boolean updateGenerationResultSelection(Long id, GenerationResultSelectionDTO dto);
}
