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
import com.ecommerce.detail.ai.util.LocalPathPolicy;
import com.ecommerce.detail.ai.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ExportServiceImpl extends ServiceImpl<ExportRecordMapper, ExportRecord> implements ExportService {

    private static final List<String> DEFAULT_EXPORT_ROOTS = List.of("exports");

    @Autowired
    private ExportUtil exportUtil;

    @Autowired
    private ProductDetailMapper productDetailMapper;

    @Autowired(required = false)
    private Environment environment;

    @Override
    public Long exportProductDetail(ExportDTO dto) {
        if (dto == null || dto.getProductDetailId() == null) {
            throw new IllegalArgumentException("Product detail ID must not be null");
        }

        ExportFormat exportFormat = ExportFormat.fromValue(dto.getExportFormat());
        if (!exportFormat.isImplemented()) {
            throw new UnsupportedOperationException("PDF export is not implemented");
        }

        // P5.3 compliance gate: audit must be approved before export
        ProductDetail detail = getProductDetailOrThrow(dto.getProductDetailId());
        Integer auditStatus = detail.getAuditStatus();
        if (auditStatus == null || auditStatus != 2) {
            throw new IllegalStateException(
                    "Export not allowed: product detail audit status is "
                    + SecurityUtil.auditStatusLabel(auditStatus)
                    + ", requires APPROVED");
        }

        ExportRecord record = new ExportRecord();
        record.setProductDetailId(dto.getProductDetailId());
        record.setExportFormat(dto.getExportFormat());
        record.setExporter(dto.getExporter());
        record.setExportTime(LocalDateTime.now());
        record.setExportStatus(0);
        this.save(record);

        try {
            String filePath = exportUtil.exportProductDetail(convertToDTO(detail), null, dto.getExportFormat());
            // P5.3: validate that the exported file lands inside allowed roots
            Path resolvedPath = LocalPathPolicy.requirePathWithinRoots(
                    filePath, allowedExportRoots(), "export output");
            record.setFilePath(resolvedPath.toString());
            record.setExportStatus(1);
            record.setErrorMessage(null);
            this.updateById(record);
        } catch (Exception e) {
            record.setExportStatus(2);
            record.setErrorMessage(SecurityUtil.scrubLocalPaths(e.getMessage()));
            this.updateById(record);
            throw new RuntimeException("Export failed", e);
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
            throw new IllegalArgumentException("Export record ID must not be null");
        }
        ExportRecord record = this.getById(id);
        if (record == null) {
            throw new com.ecommerce.detail.ai.exception.ResourceNotFoundException("Export record not found, ID: " + id);
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
            throw new IllegalArgumentException("Export record ID must not be null");
        }
        ExportRecord record = this.getById(id);
        if (record == null) {
            throw new com.ecommerce.detail.ai.exception.ResourceNotFoundException("Export record not found, ID: " + id);
        }

        // P5.3 compliance gate: re-check audit approval
        ProductDetail detail = getProductDetailOrThrow(record.getProductDetailId());
        Integer auditStatus = detail.getAuditStatus();
        if (auditStatus == null || auditStatus != 2) {
            throw new IllegalStateException(
                    "Re-export not allowed: product detail audit status is "
                    + SecurityUtil.auditStatusLabel(auditStatus)
                    + ", requires APPROVED");
        }

        record.setExportStatus(0);
        record.setErrorMessage(null);
        record.setExportTime(LocalDateTime.now());
        this.updateById(record);

        try {
            String filePath = exportUtil.exportProductDetail(convertToDTO(detail), null, record.getExportFormat());
            Path resolvedPath = LocalPathPolicy.requirePathWithinRoots(
                    filePath, allowedExportRoots(), "export output");
            record.setFilePath(resolvedPath.toString());
            record.setExportStatus(1);
            this.updateById(record);
            return true;
        } catch (Exception e) {
            record.setExportStatus(2);
            record.setErrorMessage(SecurityUtil.scrubLocalPaths(e.getMessage()));
            this.updateById(record);
            throw new RuntimeException("Re-export failed", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteExport(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Export record ID must not be null");
        }
        ExportRecord record = this.getById(id);
        if (record == null) {
            throw new com.ecommerce.detail.ai.exception.ResourceNotFoundException("Export record not found, ID: " + id);
        }
        if (record.getFilePath() != null && !record.getFilePath().trim().isEmpty()) {
            try {
                // P5.3: only delete files inside allowed roots
                Path filePath = LocalPathPolicy.requirePathWithinRoots(
                        record.getFilePath(), allowedExportRoots(), "export file");
                java.nio.file.Files.deleteIfExists(filePath);
            } catch (Exception e) {
                log.warn("Failed to delete export file");
            }
        }
        return this.removeById(id);
    }

    @Override
    public ExportRecord downloadExportFile(Long id) {
        ExportRecord record = getExportById(id);
        if (record.getExportStatus() != 1) {
            throw new IllegalStateException("Export not completed or failed, cannot download");
        }
        if (record.getFilePath() == null || record.getFilePath().trim().isEmpty()) {
            throw new IllegalStateException("Export file path is missing");
        }

        // P5.3: enforce path whitelist before serving download
        Path filePath = LocalPathPolicy.requirePathWithinRoots(
                record.getFilePath(), allowedExportRoots(), "download file");

        File file = filePath.toFile();
        if (!file.exists() || !file.isFile()) {
            throw new IllegalStateException("Export file does not exist");
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
            throw new com.ecommerce.detail.ai.exception.ResourceNotFoundException("Product detail not found, ID: " + productDetailId);
        }
        return detail;
    }

    private List<Path> allowedExportRoots() {
        String configuredRoots = environment == null
                ? ""
                : environment.getProperty("security.allowed-export-roots", "");
        return LocalPathPolicy.parseAllowedRoots(configuredRoots, DEFAULT_EXPORT_ROOTS);
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
