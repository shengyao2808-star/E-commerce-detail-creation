package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.ProductContentTaskApplyDTO;
import com.ecommerce.detail.ai.dto.ProductContentTaskDTO;
import com.ecommerce.detail.ai.dto.ProductContentTaskRequestDTO;
import com.ecommerce.detail.ai.service.ProductContentTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product-content-tasks")
public class ProductContentTaskController {

    @Autowired
    private ProductContentTaskService productContentTaskService;

    @GetMapping({"", "/list"})
    public Result<PageResult<ProductContentTaskDTO>> listProductContentTasks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long productDetailId,
            @RequestParam(required = false) String status) {
        return Result.success(productContentTaskService.listProductContentTasks(pageNum, pageSize, productDetailId, status));
    }

    @PostMapping("")
    public Result<ProductContentTaskDTO> createProductContentTask(@RequestBody ProductContentTaskRequestDTO dto) {
        return Result.success(productContentTaskService.createProductContentTask(dto));
    }

    @GetMapping("/{id}")
    public Result<ProductContentTaskDTO> getProductContentTaskById(@PathVariable Long id) {
        return Result.success(productContentTaskService.getProductContentTaskById(id));
    }

    @PostMapping("/{id}/apply")
    public Result<ProductContentTaskDTO> applyProductContentTask(@PathVariable Long id,
                                                                 @RequestBody ProductContentTaskApplyDTO dto) {
        return Result.success(productContentTaskService.applyProductContentTask(id, dto));
    }
}
