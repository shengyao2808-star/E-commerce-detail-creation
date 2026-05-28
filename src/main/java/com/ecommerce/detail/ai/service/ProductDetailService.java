package com.ecommerce.detail.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.ApplyGenerationResultsDTO;
import com.ecommerce.detail.ai.dto.DetailRiskResultDTO;
import com.ecommerce.detail.ai.dto.ProductDetailDTO;
import com.ecommerce.detail.ai.entity.ProductDetail;

import java.util.List;

/**
 * 商品详情页服务接口
 * 
 * @author Administrator
 * @version 1.0.0
 */
public interface ProductDetailService extends IService<ProductDetail> {

    /**
     * AI生成商品详情页
     * 
     * @param dto 商品详情页DTO
     * @return 商品详情页ID
     */
    Long generateProductDetail(ProductDetailDTO dto);

    /**
     * 批量AI生成商品详情页
     * 
     * @param dtos 商品详情页列表
     * @return 成功生成的数量
     */
    int batchGenerateProductDetails(List<ProductDetailDTO> dtos);

    /**
     * 根据ID获取商品详情页
     * 
     * @param id 商品详情页ID
     * @return 商品详情页
     */
    ProductDetail getProductDetailById(Long id);

    /**
     * 分页查询商品详情页列表
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param keyword 关键词
     * @param status 状态
     * @return 分页结果
     */
    PageResult<ProductDetail> listProductDetails(int pageNum, int pageSize, String keyword, Integer status);

    /**
     * 更新商品详情页
     * 
     * @param id 商品详情页ID
     * @param dto 更新信息
     * @return 是否成功
     */
    boolean updateProductDetail(Long id, ProductDetailDTO dto);

    List<String> getModuleOrder(Long id);

    boolean updateModuleOrder(Long id, List<String> moduleOrder);

    int applyGenerationResults(Long id, ApplyGenerationResultsDTO dto);

    /**
     * 删除商品详情页
     * 
     * @param id 商品详情页ID
     * @return 是否成功
     */
    boolean deleteProductDetail(Long id);

    /**
     * 批量删除商品详情页
     * 
     * @param ids ID列表
     * @return 删除数量
     */
    int batchDeleteProductDetails(List<Long> ids);

    /**
     * 审核商品详情页
     * 
     * @param id 商品详情页ID
     * @param approved 是否通过
     * @param comment 审核意见
     * @return 是否成功
     */
    boolean auditProductDetail(Long id, Boolean approved, String comment);

    /**
     * 重新生成商品详情页
     * 
     * @param id 商品详情页ID
     * @return 是否成功
     */
    boolean regenerateProductDetail(Long id);

    /**
     * 对商品详情执行风险检测并持久化结果
     *
     * @param id 商品详情ID
     * @return 风险检测结果
     */
    DetailRiskResultDTO checkProductDetailRisk(Long id);

    /**
     * 查询商品详情当前风险结果
     *
     * @param id 商品详情ID
     * @return 风险检测结果
     */
    DetailRiskResultDTO getProductDetailRisk(Long id);

    /**
     * 导出商品详情页
     * 
     * @param id 商品详情页ID
     * @param format 导出格式
     * @return 导出文件路径
     */
    String exportProductDetail(Long id, String format);
}
