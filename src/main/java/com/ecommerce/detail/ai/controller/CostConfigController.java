package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.CostConfigDTO;
import com.ecommerce.detail.ai.service.CostConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cost-configs")
public class CostConfigController {

    @Autowired
    private CostConfigService costConfigService;

    @GetMapping("/list")
    public Result<PageResult<CostConfigDTO>> listCostConfigs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String providerType,
            @RequestParam(required = false) String providerCode) {
        return Result.success(costConfigService.listCostConfigs(pageNum, pageSize, providerType, providerCode));
    }

    @PostMapping("")
    public Result<Long> createCostConfig(@RequestBody CostConfigDTO dto) {
        return Result.success(costConfigService.createCostConfig(dto));
    }

    @GetMapping("/{id}")
    public Result<CostConfigDTO> getCostConfigById(@PathVariable Long id) {
        CostConfigDTO dto = costConfigService.getCostConfigById(id);
        return dto != null ? Result.success(dto) : Result.notFound("Cost config not found");
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateCostConfig(@PathVariable Long id, @RequestBody CostConfigDTO dto) {
        return Result.success(costConfigService.updateCostConfig(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteCostConfig(@PathVariable Long id) {
        return Result.success(costConfigService.deleteCostConfig(id));
    }
}
