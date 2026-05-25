package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.DetailRiskResultDTO;
import com.ecommerce.detail.ai.dto.ProductDetailDTO;
import com.ecommerce.detail.ai.entity.ProductDetail;
import com.ecommerce.detail.ai.service.ProductDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商品详情页控制器
 * 
 * @author Administrator
 * @version 1.0.0
 */
@RestController
@RequestMapping("/detail")
public class ProductDetailController {

    @Autowired
    private ProductDetailService productDetailService;

    /**
     * AI生成商品详情页
     * 
     * @param dto 商品详情页DTO
     * @return 结果
     */
    @PostMapping("/generate")
    public Result<Long> generateProductDetail(@RequestBody ProductDetailDTO dto) {
        Long id = productDetailService.generateProductDetail(dto);
        return Result.success(id);
    }

    /**
     * 获取商品详情页详情
     * 
     * @param id 商品详情页ID
     * @return 结果
     */
    @GetMapping("/{id}")
    public Result<ProductDetail> getProductDetailById(@PathVariable Long id) {
        ProductDetail detail = productDetailService.getProductDetailById(id);
        return Result.success(detail);
    }

    @GetMapping("/list")
    public Result<PageResult<ProductDetail>> listProductDetails(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        PageResult<ProductDetail> result = productDetailService.listProductDetails(pageNum, pageSize, keyword, status);
        return Result.success(result);
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateProductDetail(@PathVariable Long id, @RequestBody ProductDetailDTO dto) {
        boolean result = productDetailService.updateProductDetail(id, dto);
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteProductDetail(@PathVariable Long id) {
        boolean result = productDetailService.deleteProductDetail(id);
        return Result.success(result);
    }

    @PostMapping("/{id}/risk-check")
    public Result<DetailRiskResultDTO> riskCheckProductDetail(@PathVariable Long id) {
        DetailRiskResultDTO result = productDetailService.checkProductDetailRisk(id);
        return Result.success(result);
    }

    @GetMapping("/{id}/risk")
    public Result<DetailRiskResultDTO> getRiskResult(@PathVariable Long id) {
        DetailRiskResultDTO result = productDetailService.getProductDetailRisk(id);
        return Result.success(result);
    }

    @PostMapping("/{id}/regenerate")
    public Result<Boolean> regenerateProductDetail(@PathVariable Long id) {
        boolean result = productDetailService.regenerateProductDetail(id);
        return Result.success(result);
    }
}
