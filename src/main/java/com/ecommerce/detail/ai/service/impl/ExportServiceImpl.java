package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.enums.ExportFormat;
import com.ecommerce.detail.ai.dto.ExportDTO;
import com.ecommerce.detail.ai.dto.ProductDetailDTO;
import com.ecommerce.detail.ai.entity.ExportRecord;
import com.ecommerce.detail.ai.entity.ProductDetail;
import com.ecommerce.detail.ai.mapper.ExportRecordMapper;
import com.ecommerce.detail.ai.mapper.ProductDetailMapper;
import com.ecommerce.detail.ai.service.ExportService;
import com.ecommerce.detail.ai.util.ExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ExportServiceImpl extends ServiceImpl<ExportRecordMapper, ExportRecord> implements ExportService {

    @Autowired
    private ExportUtil exportUtil;

    @Autowired
    private ProductDetailMapper productDetailMapper;

    @Override
    public Long exportProductDetail(ExportDTO dto) {
        if (dto == null || dto.getProductDetailId() == null) {
            throw new IllegalArgumentException("商品详情页ID不能为空");
        }

        ExportFormat exportFormat = ExportFormat.fromValue(dto.getExportFormat());
        if (!exportFormat.isImplemented()) {
            throw new UnsupportedOperationException("PDF export is not implemented");
        }

        ExportRecord record = new ExportRecord();
        record.setProductDetailId(dto.getProductDetailId());
        record.setExportFormat(dto.getExportFormat());
        record.setExporter(dto.getExporter());
        record.setExportTime(LocalDateTime.now());
        record.setExportStatus(0);
        this.save(record);

        try {
            ProductDetail detail = getProductDetailOrThrow(dto.getProductDetailId());
            String filePath = exportUtil.exportProductDetail(convertToDTO(detail), null, dto.getExportFormat());
            record.setFilePath(filePath);
            record.setExportStatus(1);
            record.setErrorMessage(null);
            this.updateById(record);
        } catch (Exception e) {
            record.setExportStatus(2);
            record.setErrorMessage(e.getMessage());
            this.updateById(record);
            throw new RuntimeException("导出失败: " + e.getMessage(), e);
        }
        return record.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchExportProductDetails(List<ExportDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return 0;
        }
        int successCount = 0;
        for (ExportDTO dto : dtos) {
            try {
                exportProductDetail(dto);
                successCount++;
            } catch (Exception e) {
                log.error("Batch export failed, productDetailId={}", dto.getProductDetailId(), e);
            }
        }
        return successCount;
    }

    @Override
    public ExportRecord getExportById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("导出记录ID不能为空");
        }
        ExportRecord record = this.getById(id);
        if (record == null) {
            throw new RuntimeException("导出记录不存在，ID: " + id);
        }
        return record;
    }

    @Override
    public PageResult<ExportRecord> listExportRecords(int pageNum, int pageSize, Integer status, String exporter) {
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        Page<ExportRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ExportRecord> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ExportRecord::getExportStatus, status);
        }
        if (exporter != null && !exporter.trim().isEmpty()) {
            wrapper.like(ExportRecord::getExporter, exporter);
        }
        wrapper.orderByDesc(ExportRecord::getExportTime);
        Page<ExportRecord> resultPage = this.page(page, wrapper);
        return new PageResult<>(resultPage.getTotal(), resultPage.getRecords(), pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reexport(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("导出记录ID不能为空");
        }
        ExportRecord record = this.getById(id);
        if (record == null) {
            throw new RuntimeException("导出记录不存在，ID: " + id);
        }

        record.setExportStatus(0);
        record.setErrorMessage(null);
        record.setExportTime(LocalDateTime.now());
        this.updateById(record);

        try {
            ProductDetail detail = getProductDetailOrThrow(record.getProductDetailId());
            String filePath = exportUtil.exportProductDetail(convertToDTO(detail), null, record.getExportFormat());
            record.setFilePath(filePath);
            record.setExportStatus(1);
            this.updateById(record);
            return true;
        } catch (Exception e) {
            record.setExportStatus(2);
            record.setErrorMessage(e.getMessage());
            this.updateById(record);
            throw new RuntimeException("重新导出失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteExport(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("导出记录ID不能为空");
        }
        ExportRecord record = this.getById(id);
        if (record == null) {
            throw new RuntimeException("导出记录不存在，ID: " + id);
        }
        if (record.getFilePath() != null && !record.getFilePath().trim().isEmpty()) {
            try {
                java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(record.getFilePath()));
            } catch (Exception e) {
                log.warn("Failed to delete export file: {}", record.getFilePath(), e);
            }
        }
        return this.removeById(id);
    }

    @Override
    public ExportRecord downloadExportFile(Long id) {
        ExportRecord record = getExportById(id);
        if (record.getExportStatus() != 1) {
            throw new RuntimeException("导出未完成或失败，无法下载");
        }
        if (record.getFilePath() == null || record.getFilePath().trim().isEmpty()) {
            throw new RuntimeException("导出文件路径不存在");
        }

        File file = new File(record.getFilePath());
        if (!file.exists() || !file.isFile()) {
            throw new RuntimeException("导出文件不存在，路径: " + record.getFilePath());
        }
        if (record.getFileName() == null || record.getFileName().trim().isEmpty()) {
            record.setFileName(file.getName());
        }
        if (record.getFileSize() == null) {
            record.setFileSize(file.length());
        }
        return record;
    }

    private ProductDetail getProductDetailOrThrow(Long productDetailId) {
        ProductDetail detail = productDetailMapper.selectById(productDetailId);
        if (detail == null) {
            throw new RuntimeException("商品详情页不存在，ID: " + productDetailId);
        }
        return detail;
    }

    private ProductDetailDTO convertToDTO(ProductDetail detail) {
        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setProductId(detail.getId());
        dto.setMaterialId(detail.getMaterialId());
        dto.setBrandId(detail.getBrandId());
        dto.setProductName(detail.getProductName() != null ? detail.getProductName() : detail.getTitle());
        dto.setTitle(detail.getTitle());
        dto.setSubtitle(detail.getSubtitle());
        dto.setCategory(detail.getCategory());
        dto.setSku(detail.getSku());
        dto.setPrice(detail.getPrice());
        dto.setDescription(detail.getDescription());
        dto.setAiGeneratedContent(detail.getAiGeneratedContent());
        dto.setCreator(detail.getCreator());

        if (detail.getSellingPoints() != null && !detail.getSellingPoints().isEmpty()) {
            dto.setSellingPoints(Arrays.asList(detail.getSellingPoints().split(",")));
        }
        if (detail.getSeoKeywords() != null && !detail.getSeoKeywords().isEmpty()) {
            dto.setSeoKeywords(Arrays.asList(detail.getSeoKeywords().split(",")));
        }
        if (detail.getImages() != null && !detail.getImages().isEmpty()) {
            dto.setImages(Arrays.asList(detail.getImages().split(",")));
        }
        if (detail.getVideos() != null && !detail.getVideos().isEmpty()) {
            dto.setVideos(Arrays.asList(detail.getVideos().split(",")));
        }
        if (detail.getDocuments() != null && !detail.getDocuments().isEmpty()) {
            dto.setDocuments(Arrays.asList(detail.getDocuments().split(",")));
        }
        return dto;
    }
}
