package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.DetailCompositionCreateDTO;
import com.ecommerce.detail.ai.dto.DetailCompositionDTO;
import com.ecommerce.detail.ai.dto.DetailCompositionListQuery;
import com.ecommerce.detail.ai.dto.DetailCompositionQualityCheckDTO;
import com.ecommerce.detail.ai.dto.DetailDeliveryManifestDTO;
import com.ecommerce.detail.ai.service.DetailCompositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@RestController
@RequestMapping("/detail-compositions")
public class DetailCompositionController {

    @Autowired
    private DetailCompositionService detailCompositionService;

    @GetMapping("/list")
    public Result<PageResult<DetailCompositionDTO>> listDetailCompositions(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long productDetailId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String toolCode,
            @RequestParam(required = false) String keyword) {
        DetailCompositionListQuery query = new DetailCompositionListQuery();
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        query.setProductDetailId(productDetailId);
        query.setStatus(status);
        query.setToolCode(toolCode);
        query.setKeyword(keyword);
        return Result.success(detailCompositionService.listDetailCompositions(query));
    }

    @PostMapping("")
    public Result<Long> createDetailComposition(@RequestBody DetailCompositionCreateDTO dto) {
        return Result.success(detailCompositionService.createDetailComposition(dto));
    }

    @GetMapping("/{id}")
    public Result<DetailCompositionDTO> getDetailCompositionById(@PathVariable Long id) {
        return Result.success(detailCompositionService.getDetailCompositionById(id));
    }

    @PostMapping("/{id}/quality-checks")
    public Result<Long> createQualityCheck(@PathVariable Long id) {
        return Result.success(detailCompositionService.createQualityCheck(id));
    }

    @GetMapping("/{id}/quality-checks/list")
    public Result<PageResult<DetailCompositionQualityCheckDTO>> listQualityChecks(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(detailCompositionService.listQualityChecks(id, pageNum, pageSize));
    }

    @GetMapping("/{id}/delivery-manifest")
    public Result<DetailDeliveryManifestDTO> getDeliveryManifest(@PathVariable Long id) {
        return Result.success(detailCompositionService.getDeliveryManifest(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDetailCompositionFile(@PathVariable Long id) {
        DetailCompositionDTO detail = detailCompositionService.getDetailCompositionById(id);
        File file = detailCompositionService.resolveDownloadFile(id);
        String fileName = detail.getOutputFileName() != null && !detail.getOutputFileName().trim().isEmpty()
                ? detail.getOutputFileName()
                : file.getName();
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        MediaType mediaType = MediaType.IMAGE_PNG;
        if (detail.getMimeType() != null && !detail.getMimeType().trim().isEmpty()) {
            try {
                mediaType = MediaType.parseMediaType(detail.getMimeType());
            } catch (Exception ignored) {
                mediaType = MediaType.IMAGE_PNG;
            }
        }
        long contentLength;
        try {
            contentLength = Files.size(file.toPath());
        } catch (Exception e) {
            contentLength = file.length();
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                .body(new FileSystemResource(file));
    }
}
