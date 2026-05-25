package com.ecommerce.detail.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.ProductMaterialDTO;
import com.ecommerce.detail.ai.entity.ProductMaterial;

import java.util.List;

/**
 * 商品素材服务接口
 * 
 * @author Administrator
 * @version 1.0.0
 */
public interface ProductMaterialService extends IService<ProductMaterial> {

    /**
     * 上传商品素材
     * 
     * @param dto 素材信息
     * @return 素材ID
     */
    Long uploadMaterial(ProductMaterialDTO dto);

    /**
     * 批量上传商品素材
     * 
     * @param dtos 素材列表
     * @return 成功上传的数量
     */
    int batchUploadMaterials(List<ProductMaterialDTO> dtos);

    /**
     * 根据ID获取素材
     * 
     * @param id 素材ID
     * @return 素材信息
     */
    ProductMaterial getMaterialById(Long id);

    /**
     * 分页查询素材列表
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param keyword 关键词
     * @return 分页结果
     */
    PageResult<ProductMaterial> listMaterials(int pageNum, int pageSize, String keyword);

    /**
     * 更新素材信息
     * 
     * @param id 素材ID
     * @param dto 更新信息
     * @return 是否成功
     */
    boolean updateMaterial(Long id, ProductMaterialDTO dto);

    /**
     * 删除素材
     * 
     * @param id 素材ID
     * @return 是否成功
     */
    boolean deleteMaterial(Long id);

    /**
     * 批量删除素材
     * 
     * @param ids ID列表
     * @return 删除数量
     */
    int batchDeleteMaterials(List<Long> ids);

    /**
     * 解析素材内容（OCR、文本提取等）
     * 
     * @param id 素材ID
     * @return 解析后的文本内容
     */
    String parseMaterialContent(Long id);

    /**
     * 验证素材有效性
     * 
     * @param id 素材ID
     * @return 是否有效
     */
    boolean validateMaterial(Long id);
}
