package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.enums.ExportFormat;
import com.ecommerce.detail.ai.dto.ExportDTO;
import com.ecommerce.detail.ai.entity.ExportRecord;
import com.ecommerce.detail.ai.service.ExportService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 导出控制器
 * 
 * @author Administrator
 * @version 1.0.0
 */
@RestController
@RequestMapping("/export")
public class ExportController {

    @Autowired
    private ExportService exportService;

    /**
     * 导出商品详情页
     * 
     * @param dto 导出DTO
     * @return 结果
     */
    @PostMapping("/export")
    public Result<Long> exportProductDetail(@RequestBody ExportDTO dto) {
        Long id = exportService.exportProductDetail(dto);
        return Result.success(id);
    }

    /**
     * 获取导出记录详情
     * 
     * @param id 导出记录ID
     * @return 结果
     */
    @GetMapping("/{id}")
    public Result<ExportRecord> getExportById(@PathVariable Long id) {
        ExportRecord record = exportService.getExportById(id);
        return Result.success(record);
    }

    @GetMapping("/list")
    public PageResult<ExportRecord> listExportRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String exporter) {
        return exportService.listExportRecords(pageNum, pageSize, status, exporter);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadExportFile(@PathVariable Long id) {
        ExportRecord record = exportService.downloadExportFile(id);
        File file = new File(record.getFilePath());
        Resource resource = new FileSystemResource(file);
        String fileName = record.getFileName() != null && !record.getFileName().trim().isEmpty()
                ? record.getFileName()
                : file.getName();
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (record.getExportFormat() != null) {
            try {
                mediaType = MediaType.parseMediaType(ExportFormat.fromValue(record.getExportFormat()).getMimeType());
            } catch (Exception ignored) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()))
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteExport(@PathVariable Long id) {
        boolean result = exportService.deleteExport(id);
        return Result.success(result);
    }

    @PostMapping("/{id}/reexport")
    public Result<Boolean> reexport(@PathVariable Long id) {
        boolean result = exportService.reexport(id);
        return Result.success(result);
    }
}
