package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.GenerationResultDTO;
import com.ecommerce.detail.ai.dto.GenerationResultListQuery;
import com.ecommerce.detail.ai.dto.GenerationResultSelectionDTO;
import com.ecommerce.detail.ai.service.GenerationResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/generation-results")
public class GenerationResultController {

    @Autowired
    private GenerationResultService generationResultService;

    @GetMapping("/list")
    public Result<PageResult<GenerationResultDTO>> listGenerationResults(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long imageJobId,
            @RequestParam(required = false) Boolean selected,
            @RequestParam(required = false) String complianceStatus) {
        GenerationResultListQuery query = new GenerationResultListQuery();
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        query.setImageJobId(imageJobId);
        query.setSelected(selected);
        query.setComplianceStatus(complianceStatus);
        return Result.success(generationResultService.listGenerationResults(query));
    }

    @GetMapping("/{id}")
    public Result<GenerationResultDTO> getGenerationResultById(@PathVariable Long id) {
        return Result.success(generationResultService.getGenerationResultById(id));
    }

    @PutMapping("/{id}/selection")
    public Result<Boolean> updateGenerationResultSelection(
            @PathVariable Long id,
            @RequestBody GenerationResultSelectionDTO dto) {
        return Result.success(generationResultService.updateGenerationResultSelection(id, dto));
    }
}
