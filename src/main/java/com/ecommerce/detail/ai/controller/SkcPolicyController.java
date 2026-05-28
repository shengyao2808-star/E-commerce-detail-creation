package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.SkcPolicyDTO;
import com.ecommerce.detail.ai.service.SkcPolicyService;
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
@RequestMapping("/skc-policies")
public class SkcPolicyController {

    @Autowired
    private SkcPolicyService skcPolicyService;

    @GetMapping("/list")
    public Result<PageResult<SkcPolicyDTO>> listSkcPolicies(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.success(skcPolicyService.listSkcPolicies(pageNum, pageSize, keyword, status));
    }

    @PostMapping("")
    public Result<Long> createSkcPolicy(@RequestBody SkcPolicyDTO dto) {
        return Result.success(skcPolicyService.createSkcPolicy(dto));
    }

    @GetMapping("/{id}")
    public Result<SkcPolicyDTO> getSkcPolicyById(@PathVariable Long id) {
        return Result.success(skcPolicyService.getSkcPolicyById(id));
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateSkcPolicy(@PathVariable Long id, @RequestBody SkcPolicyDTO dto) {
        return Result.success(skcPolicyService.updateSkcPolicy(id, dto));
    }

    @PostMapping("/{id}/confirm")
    public Result<SkcPolicyDTO> confirmSkcPolicy(@PathVariable Long id) {
        return Result.success(skcPolicyService.confirmSkcPolicy(id));
    }
}
