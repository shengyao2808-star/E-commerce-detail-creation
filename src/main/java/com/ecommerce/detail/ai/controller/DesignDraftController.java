package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.DesignDraftDTO;
import com.ecommerce.detail.ai.service.DesignDraftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/design-drafts")
public class DesignDraftController {

    @Autowired
    private DesignDraftService designDraftService;

    @GetMapping("/list")
    public Result<PageResult<DesignDraftDTO>> listDesignDrafts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long productDetailId,
            @RequestParam(required = false) Long productMaterialId,
            @RequestParam(required = false) String status) {
        return Result.success(designDraftService.listDesignDrafts(pageNum, pageSize, productDetailId, productMaterialId, status));
    }

    @PostMapping("")
    public Result<Long> createDesignDraft(@RequestBody DesignDraftDTO dto) {
        return Result.success(designDraftService.createDesignDraft(dto));
    }

    @GetMapping("/{id}")
    public Result<DesignDraftDTO> getDesignDraftById(@PathVariable Long id) {
        return Result.success(designDraftService.getDesignDraftById(id));
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateDesignDraft(@PathVariable Long id, @RequestBody DesignDraftDTO dto) {
        return Result.success(designDraftService.updateDesignDraft(id, dto));
    }
}
