package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.ProductMaterialDTO;
import com.ecommerce.detail.ai.entity.ProductMaterial;
import com.ecommerce.detail.ai.mapper.ProductMaterialMapper;
import com.ecommerce.detail.ai.service.ProductMaterialService;
import com.ecommerce.detail.ai.util.AIUtil;
import com.ecommerce.detail.ai.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品素材服务实现类
 * 
 * @author Administrator
 * @version 1.0.0
 */
@Slf4j
@Service
public class ProductMaterialServiceImpl extends ServiceImpl<ProductMaterialMapper, ProductMaterial> implements ProductMaterialService {

    @Autowired
    private AIUtil aiUtil;

    @Override
    public Long uploadMaterial(ProductMaterialDTO dto) {
        // 参数校验
        if (dto == null || dto.getProductName() == null || dto.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("商品名称不能为空");
        }

        ProductMaterial material = new ProductMaterial();
        material.setProductName(dto.getProductName());
        material.setCategory(dto.getCategory());
        material.setProductSku(dto.getSku());
        material.setPrice(dto.getPrice());
        material.setDescription(dto.getDescription());
        material.setImages(dto.getImages());
        material.setVideos(dto.getVideos());
        material.setDocuments(dto.getDocuments());
        material.setUploader(dto.getUploader());
        material.setUploadTime(LocalDateTime.now());
        material.setStatus(1);
        
        this.save(material);
        log.info("上传商品素材成功，ID: {}, 商品名: {}", material.getId(), material.getProductName());
        return material.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUploadMaterials(List<ProductMaterialDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (ProductMaterialDTO dto : dtos) {
            try {
                uploadMaterial(dto);
                successCount++;
            } catch (Exception e) {
                log.error("批量上传素材失败，商品名: {}", dto.getProductName(), e);
            }
        }
        
        log.info("批量上传完成，总数: {}, 成功: {}", dtos.size(), successCount);
        return successCount;
    }

    @Override
    public ProductMaterial getMaterialById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        
        ProductMaterial material = this.getById(id);
        if (material == null) {
            throw new RuntimeException("素材不存在，ID: " + id);
        }
        
        return material;
    }

    @Override
    public PageResult<ProductMaterial> listMaterials(int pageNum, int pageSize, String keyword) {
        // 参数校验和默认值设置
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        Page<ProductMaterial> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProductMaterial> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(ProductMaterial::getProductName, keyword)
                   .or()
                   .like(ProductMaterial::getProductSku, keyword)
                   .or()
                   .like(ProductMaterial::getCategory, keyword);
        }
        
        // 按上传时间倒序排列
        wrapper.orderByDesc(ProductMaterial::getUploadTime);
        
        Page<ProductMaterial> resultPage = this.page(page, wrapper);
        
        return new PageResult<>(resultPage.getTotal(), resultPage.getRecords(), pageNum, pageSize);
    }

    @Override
    public boolean updateMaterial(Long id, ProductMaterialDTO dto) {
        if (id == null || dto == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        ProductMaterial existing = this.getById(id);
        if (existing == null) {
            throw new RuntimeException("素材不存在，ID: " + id);
        }

        // 更新字段
        if (dto.getProductName() != null) {
            existing.setProductName(dto.getProductName());
        }
        if (dto.getCategory() != null) {
            existing.setCategory(dto.getCategory());
        }
        if (dto.getSku() != null) {
            existing.setProductSku(dto.getSku());
        }
        if (dto.getPrice() != null) {
            existing.setPrice(dto.getPrice());
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getImages() != null) {
            existing.setImages(dto.getImages());
        }
        if (dto.getVideos() != null) {
            existing.setVideos(dto.getVideos());
        }
        if (dto.getDocuments() != null) {
            existing.setDocuments(dto.getDocuments());
        }

        boolean result = this.updateById(existing);
        log.info("更新商品素材{}，ID: {}", result ? "成功" : "失败", id);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMaterial(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID不能为空");
        }

        ProductMaterial material = this.getById(id);
        if (material == null) {
            throw new RuntimeException("素材不存在，ID: " + id);
        }

        // 删除关联文件（如果有）
        if (material.getImages() != null) {
            FileUtil.deleteFiles(material.getImages());
        }
        if (material.getVideos() != null) {
            FileUtil.deleteFiles(material.getVideos());
        }
        if (material.getDocuments() != null) {
            FileUtil.deleteFiles(material.getDocuments());
        }

        boolean result = this.removeById(id);
        log.info("删除商品素材{}，ID: {}", result ? "成功" : "失败", id);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteMaterials(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        int deleteCount = 0;
        for (Long id : ids) {
            try {
                if (deleteMaterial(id)) {
                    deleteCount++;
                }
            } catch (Exception e) {
                log.error("批量删除素材失败，ID: {}", id, e);
            }
        }
        
        log.info("批量删除完成，总数: {}, 成功: {}", ids.size(), deleteCount);
        return deleteCount;
    }

    @Override
    public String parseMaterialContent(Long id) {
        ProductMaterial material = getMaterialById(id);
        
        StringBuilder content = new StringBuilder();
        
        // 解析图片（OCR）
        if (material.getImages() != null && !material.getImages().isEmpty()) {
            String ocrText = FileUtil.extractTextFromImages(material.getImages());
            if (ocrText != null && !ocrText.isEmpty()) {
                content.append("【图片内容】\n").append(ocrText).append("\n\n");
            }
        }
        
        // 解析文档
        if (material.getDocuments() != null && !material.getDocuments().isEmpty()) {
            String docText = FileUtil.extractTextFromDocuments(material.getDocuments());
            if (docText != null && !docText.isEmpty()) {
                content.append("【文档内容】\n").append(docText).append("\n\n");
            }
        }
        
        // 添加商品基本信息
        content.append("【商品信息】\n")
               .append("商品名称: ").append(material.getProductName()).append("\n")
               .append("分类: ").append(material.getCategory()).append("\n")
               .append("SKU: ").append(material.getProductSku()).append("\n")
               .append("价格: ").append(material.getPrice()).append("\n")
               .append("描述: ").append(material.getDescription());
        
        return content.toString();
    }

    @Override
    public boolean validateMaterial(Long id) {
        ProductMaterial material = getMaterialById(id);
        
        // 验证必填字段
        if (material.getProductName() == null || material.getProductName().trim().isEmpty()) {
            log.warn("素材验证失败：商品名称为空，ID: {}", id);
            return false;
        }
        
        if (material.getProductSku() == null || material.getProductSku().trim().isEmpty()) {
            log.warn("素材验证失败：SKU为空，ID: {}", id);
            return false;
        }
        
        // 验证文件格式（如果有文件）
        if (material.getImages() != null && !material.getImages().isEmpty()) {
            if (!FileUtil.validateImageFiles(material.getImages())) {
                log.warn("素材验证失败：图片格式不正确，ID: {}", id);
                return false;
            }
        }
        
        if (material.getVideos() != null && !material.getVideos().isEmpty()) {
            if (!FileUtil.validateVideoFiles(material.getVideos())) {
                log.warn("素材验证失败：视频格式不正确，ID: {}", id);
                return false;
            }
        }
        
        log.info("素材验证通过，ID: {}", id);
        return true;
    }
}
