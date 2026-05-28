package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.PromptTemplateCreateDTO;
import com.ecommerce.detail.ai.dto.PromptTemplateDTO;
import com.ecommerce.detail.ai.service.PromptTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/prompt-templates")
public class PromptTemplateController {

    @Autowired
    private PromptTemplateService promptTemplateService;

    @GetMapping({"", "/list"})
    public Result<PageResult<PromptTemplateDTO>> listTemplates(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String style,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String keyword) {
        return Result.success(promptTemplateService.listTemplates(pageNum, pageSize, category, platform, style, source, keyword));
    }

    @GetMapping("/{id}")
    public Result<PromptTemplateDTO> getTemplateById(@PathVariable Long id) {
        return Result.success(promptTemplateService.getTemplateById(id));
    }

    @PostMapping
    public Result<PromptTemplateDTO> createTemplate(@RequestBody PromptTemplateCreateDTO dto) {
        return Result.success(promptTemplateService.createTemplate(dto));
    }

    @PutMapping("/{id}")
    public Result<PromptTemplateDTO> updateTemplate(@PathVariable Long id, @RequestBody PromptTemplateCreateDTO dto) {
        return Result.success(promptTemplateService.updateTemplate(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        promptTemplateService.deleteTemplate(id);
        return Result.success(null);
    }

    @PostMapping("/{id}/duplicate")
    public Result<PromptTemplateDTO> duplicateTemplate(@PathVariable Long id) {
        return Result.success(promptTemplateService.duplicateTemplate(id));
    }

    @PostMapping("/{id}/use")
    public Result<Void> useTemplate(@PathVariable Long id) {
        promptTemplateService.incrementUsageCount(id);
        return Result.success(null);
    }
}