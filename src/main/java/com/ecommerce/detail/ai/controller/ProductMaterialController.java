package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.ProductMaterialDTO;
import com.ecommerce.detail.ai.entity.ProductMaterial;
import com.ecommerce.detail.ai.service.ProductMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商品资料控制器
 * 
 * @author Administrator
 * @version 1.0.0
 */
@RestController
@RequestMapping("/material")
public class ProductMaterialController {

    @Autowired
    private ProductMaterialService productMaterialService;

    /**
     * 上传商品资料
     * 
     * @param dto 商品资料DTO
     * @return 结果
     */
    @PostMapping("/upload")
    public Result<Long> uploadMaterial(@RequestBody ProductMaterialDTO dto) {
        Long id = productMaterialService.uploadMaterial(dto);
        return Result.success(id);
    }

    /**
     * 获取商品资料详情
     * 
     * @param id 商品资料ID
     * @return 结果
     */
    @GetMapping("/{id}")
    public Result<ProductMaterial> getMaterialById(@PathVariable Long id) {
        ProductMaterial material = productMaterialService.getMaterialById(id);
        return Result.success(material);
    }

    @GetMapping("/list")
    public Result<PageResult<ProductMaterial>> listMaterials(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        PageResult<ProductMaterial> result = productMaterialService.listMaterials(pageNum, pageSize, keyword);
        return Result.success(result);
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateMaterial(@PathVariable Long id, @RequestBody ProductMaterialDTO dto) {
        boolean result = productMaterialService.updateMaterial(id, dto);
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteMaterial(@PathVariable Long id) {
        boolean result = productMaterialService.deleteMaterial(id);
        return Result.success(result);
    }
}
