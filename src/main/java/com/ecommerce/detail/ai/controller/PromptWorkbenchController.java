package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.PromptWorkbenchEntryDTO;
import com.ecommerce.detail.ai.dto.PromptWorkbenchRequestDTO;
import com.ecommerce.detail.ai.service.PromptWorkbenchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/prompt-workbench")
public class PromptWorkbenchController {

    @Autowired
    private PromptWorkbenchService promptWorkbenchService;

    @GetMapping({"", "/list"})
    public Result<PageResult<PromptWorkbenchEntryDTO>> listPromptWorkbenchEntries(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String entryType,
            @RequestParam(required = false) String status) {
        return Result.success(promptWorkbenchService.listPromptWorkbenchEntries(pageNum, pageSize, entryType, status));
    }

    @GetMapping("/{id}")
    public Result<PromptWorkbenchEntryDTO> getPromptWorkbenchEntryById(@PathVariable Long id) {
        return Result.success(promptWorkbenchService.getPromptWorkbenchEntryById(id));
    }

    @PostMapping("/guided")
    public Result<PromptWorkbenchEntryDTO> createGuidedPrompt(@RequestBody PromptWorkbenchRequestDTO dto) {
        return Result.success(promptWorkbenchService.createGuidedPrompt(dto));
    }

    @PostMapping("/expand")
    public Result<PromptWorkbenchEntryDTO> expandPrompt(@RequestBody PromptWorkbenchRequestDTO dto) {
        return Result.success(promptWorkbenchService.expandPrompt(dto));
    }

    @PostMapping("/image-to-prompt")
    public Result<PromptWorkbenchEntryDTO> imageToPrompt(@RequestBody PromptWorkbenchRequestDTO dto) {
        return Result.success(promptWorkbenchService.imageToPrompt(dto));
    }
}
