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
import com.ecommerce.detail.ai.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Product material service.
 * P5.3: validates file paths for traversal and allowed file types on upload.
 */
@Slf4j
@Service
public class ProductMaterialServiceImpl extends ServiceImpl<ProductMaterialMapper, ProductMaterial> implements ProductMaterialService {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg");
    private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = Set.of(
            "mp4", "avi", "mov", "wmv", "flv", "mkv", "webm");
    private static final Set<String> ALLOWED_DOCUMENT_EXTENSIONS = Set.of(
            "txt", "doc", "docx", "pdf", "csv", "xls", "xlsx", "ppt", "pptx", "md");

    @Autowired
    private AIUtil aiUtil;

    @Override
    public Long uploadMaterial(ProductMaterialDTO dto) {
        if (dto == null || dto.getProductName() == null || dto.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name must not be empty");
        }

        // P5.3: validate file paths for traversal and file types
        validateFilePaths(dto);

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
        log.info("Upload material success, ID: {}", material.getId());
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
                log.error("Batch upload material failed", e);
            }
        }
        log.info("Batch upload completed, total: {}, success: {}", dtos.size(), successCount);
        return successCount;
    }

    @Override
    public ProductMaterial getMaterialById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID must not be null");
        }
        ProductMaterial material = this.getById(id);
        if (material == null) {
            throw new RuntimeException("Material not found, ID: " + id);
        }
        return material;
    }

    @Override
    public PageResult<ProductMaterial> listMaterials(int pageNum, int pageSize, String keyword) {
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        Page<ProductMaterial> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProductMaterial> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(ProductMaterial::getProductName, keyword)
                   .or()
                   .like(ProductMaterial::getProductSku, keyword)
                   .or()
                   .like(ProductMaterial::getCategory, keyword);
        }
        wrapper.orderByDesc(ProductMaterial::getUploadTime);
        Page<ProductMaterial> resultPage = this.page(page, wrapper);
        return new PageResult<>(resultPage.getTotal(), resultPage.getRecords(), pageNum, pageSize);
    }

    @Override
    public boolean updateMaterial(Long id, ProductMaterialDTO dto) {
        if (id == null || dto == null) {
            throw new IllegalArgumentException("Parameters must not be null");
        }

        ProductMaterial existing = this.getById(id);
        if (existing == null) {
            throw new RuntimeException("Material not found, ID: " + id);
        }

        // P5.3: validate updated file paths
        validateFilePaths(dto);

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
        log.info("Update material {}, ID: {}", result ? "success" : "failed", id);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMaterial(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID must not be null");
        }
        ProductMaterial material = this.getById(id);
        if (material == null) {
            throw new RuntimeException("Material not found, ID: " + id);
        }
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
        log.info("Delete material {}, ID: {}", result ? "success" : "failed", id);
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
                log.error("Batch delete material failed, ID: {}", id, e);
            }
        }
        log.info("Batch delete completed, total: {}, success: {}", ids.size(), deleteCount);
        return deleteCount;
    }

    @Override
    public String parseMaterialContent(Long id) {
        ProductMaterial material = getMaterialById(id);
        StringBuilder content = new StringBuilder();

        if (material.getImages() != null && !material.getImages().isEmpty()) {
            String ocrText = FileUtil.extractTextFromImages(material.getImages());
            if (ocrText != null && !ocrText.isEmpty()) {
                content.append("[Image Content]\n").append(ocrText).append("\n\n");
            }
        }

        if (material.getDocuments() != null && !material.getDocuments().isEmpty()) {
            String docText = FileUtil.extractTextFromDocuments(material.getDocuments());
            if (docText != null && !docText.isEmpty()) {
                content.append("[Document Content]\n").append(docText).append("\n\n");
            }
        }

        content.append("[Product Info]\n")
               .append("Name: ").append(material.getProductName()).append("\n")
               .append("Category: ").append(material.getCategory()).append("\n")
               .append("SKU: ").append(material.getProductSku()).append("\n")
               .append("Price: ").append(material.getPrice()).append("\n")
               .append("Description: ").append(material.getDescription());

        return content.toString();
    }

    @Override
    public boolean validateMaterial(Long id) {
        ProductMaterial material = getMaterialById(id);

        if (material.getProductName() == null || material.getProductName().trim().isEmpty()) {
            log.warn("Material validation failed: product name is empty, ID: {}", id);
            return false;
        }
        if (material.getProductSku() == null || material.getProductSku().trim().isEmpty()) {
            log.warn("Material validation failed: SKU is empty, ID: {}", id);
            return false;
        }
        if (material.getImages() != null && !material.getImages().isEmpty()) {
            if (!FileUtil.validateImageFiles(material.getImages())) {
                log.warn("Material validation failed: image format invalid, ID: {}", id);
                return false;
            }
        }
        if (material.getVideos() != null && !material.getVideos().isEmpty()) {
            if (!FileUtil.validateVideoFiles(material.getVideos())) {
                log.warn("Material validation failed: video format invalid, ID: {}", id);
                return false;
            }
        }
        log.info("Material validation passed, ID: {}", id);
        return true;
    }

    // ── P5.3 internal validation ──────────────────────────────────

    /**
     * Validates all file path lists in the DTO for path traversal and
     * file extension allowlist.
     */
    private void validateFilePaths(ProductMaterialDTO dto) {
        if (dto.getImages() != null) {
            SecurityUtil.rejectPathTraversalInList(dto.getImages(), "image path");
            for (String img : dto.getImages()) {
                SecurityUtil.requireAllowedExtension(img, ALLOWED_IMAGE_EXTENSIONS, "image file");
            }
        }
        if (dto.getVideos() != null) {
            SecurityUtil.rejectPathTraversalInList(dto.getVideos(), "video path");
            for (String vid : dto.getVideos()) {
                SecurityUtil.requireAllowedExtension(vid, ALLOWED_VIDEO_EXTENSIONS, "video file");
            }
        }
        if (dto.getDocuments() != null) {
            SecurityUtil.rejectPathTraversalInList(dto.getDocuments(), "document path");
            for (String doc : dto.getDocuments()) {
                SecurityUtil.requireAllowedExtension(doc, ALLOWED_DOCUMENT_EXTENSIONS, "document file");
            }
        }
    }
}
