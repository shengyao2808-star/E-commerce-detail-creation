package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.ModelProfileDTO;
import com.ecommerce.detail.ai.service.ModelProfileService;
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
@RequestMapping("/model-profiles")
public class ModelProfileController {

    @Autowired
    private ModelProfileService modelProfileService;

    @GetMapping({"", "/list"})
    public Result<PageResult<ModelProfileDTO>> listModelProfiles(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.success(modelProfileService.listModelProfiles(pageNum, pageSize, keyword, status));
    }

    @PostMapping("")
    public Result<Long> createModelProfile(@RequestBody ModelProfileDTO dto) {
        return Result.success(modelProfileService.createModelProfile(dto));
    }

    @GetMapping("/{id}")
    public Result<ModelProfileDTO> getModelProfileById(@PathVariable Long id) {
        return Result.success(modelProfileService.getModelProfileById(id));
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateModelProfile(@PathVariable Long id, @RequestBody ModelProfileDTO dto) {
        return Result.success(modelProfileService.updateModelProfile(id, dto));
    }

    @PostMapping("/{id}/confirm")
    public Result<ModelProfileDTO> confirmModelProfile(@PathVariable Long id) {
        return Result.success(modelProfileService.confirmModelProfile(id));
    }
}
