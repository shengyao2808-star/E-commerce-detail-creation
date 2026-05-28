package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.CategoryVisualPolicyDTO;
import com.ecommerce.detail.ai.service.CategoryVisualPolicyService;
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
@RequestMapping("/category-visual-policies")
public class CategoryVisualPolicyController {

    @Autowired
    private CategoryVisualPolicyService categoryVisualPolicyService;

    @GetMapping({"", "/list"})
    public Result<PageResult<CategoryVisualPolicyDTO>> listCategoryVisualPolicies(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.success(categoryVisualPolicyService.listCategoryVisualPolicies(pageNum, pageSize, keyword, status));
    }

    @PostMapping("")
    public Result<Long> createCategoryVisualPolicy(@RequestBody CategoryVisualPolicyDTO dto) {
        return Result.success(categoryVisualPolicyService.createCategoryVisualPolicy(dto));
    }

    @GetMapping("/{id}")
    public Result<CategoryVisualPolicyDTO> getCategoryVisualPolicyById(@PathVariable Long id) {
        return Result.success(categoryVisualPolicyService.getCategoryVisualPolicyById(id));
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateCategoryVisualPolicy(@PathVariable Long id, @RequestBody CategoryVisualPolicyDTO dto) {
        return Result.success(categoryVisualPolicyService.updateCategoryVisualPolicy(id, dto));
    }

    @PostMapping("/{id}/confirm")
    public Result<CategoryVisualPolicyDTO> confirmCategoryVisualPolicy(@PathVariable Long id) {
        return Result.success(categoryVisualPolicyService.confirmCategoryVisualPolicy(id));
    }
}
