package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.MaterialParseTaskDTO;
import com.ecommerce.detail.ai.dto.ProductLinkPreviewDTO;
import com.ecommerce.detail.ai.dto.ProductMaterialFileUploadResponseDTO;
import com.ecommerce.detail.ai.dto.ProductMaterialDTO;
import com.ecommerce.detail.ai.entity.ProductMaterial;
import com.ecommerce.detail.ai.mapper.ProductMaterialMapper;
import com.ecommerce.detail.ai.security.ProductResourceOwnershipGuard;
import com.ecommerce.detail.ai.service.linkpreview.DomesticEcommerceProductLinkPreviewSource;
import com.ecommerce.detail.ai.service.MaterialParseTaskService;
import com.ecommerce.detail.ai.service.ProductMaterialService;
import com.ecommerce.detail.ai.service.linkpreview.ProductLinkPreviewContext;
import com.ecommerce.detail.ai.service.linkpreview.ProductLinkPreviewHtmlSupport;
import com.ecommerce.detail.ai.service.linkpreview.ProductLinkPreviewSource;
import com.ecommerce.detail.ai.service.linkpreview.WebsiteMetadataProductLinkPreviewSource;
import com.ecommerce.detail.ai.util.FileUtil;
import com.ecommerce.detail.ai.util.LocalPathPolicy;
import com.ecommerce.detail.ai.util.SecurityUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
    private static final Set<String> V1_UPLOAD_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "pdf", "zip");
    private static final Set<String> V1_UPLOAD_IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp");
    private static final long DEFAULT_MATERIAL_UPLOAD_MAX_BYTES = 200L * 1024 * 1024;
    private static final int DEFAULT_MATERIAL_UPLOAD_MAX_COUNT = 40;
    private static final Duration LINK_PREVIEW_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration LINK_PREVIEW_REQUEST_TIMEOUT = Duration.ofSeconds(8);
    private static final Set<String> PRIVATE_HOST_NAMES = Set.of("localhost", "127.0.0.1", "::1", "0.0.0.0");
    private static final Set<String> TRACKING_QUERY_KEYS = Set.of(
            "mi_id", "pvid", "scm", "spm", "utparam", "xxc",
            "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content",
            "ali_trackid", "traceid", "trackid", "from", "scene");
    private static final Map<String, String> PLATFORM_HOST_MAP = Map.ofEntries(
            Map.entry("taobao.com", "淘宝 / 天猫"),
            Map.entry("tmall.com", "淘宝 / 天猫"),
            Map.entry("1688.com", "1688"),
            Map.entry("jd.com", "京东"),
            Map.entry("jingxi.com", "京东"),
            Map.entry("pinduoduo.com", "拼多多"),
            Map.entry("yangkeduo.com", "拼多多"),
            Map.entry("douyin.com", "抖音小店"),
            Map.entry("jinritemai.com", "抖音小店")
    );

    @Autowired(required = false)
    private Environment environment;

    @Autowired(required = false)
    private ProductResourceOwnershipGuard ownershipGuard;

    @Autowired(required = false)
    private MaterialParseTaskService materialParseTaskService;

    private final HttpClient linkPreviewHttpClient;
    private final ObjectMapper objectMapper;
    private final ProductLinkPreviewHtmlSupport linkPreviewHtmlSupport;
    private final List<ProductLinkPreviewSource> linkPreviewSources;

    public ProductMaterialServiceImpl() {
        this.linkPreviewHttpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(LINK_PREVIEW_CONNECT_TIMEOUT)
                .build();
        this.objectMapper = new ObjectMapper();
        this.linkPreviewHtmlSupport = new ProductLinkPreviewHtmlSupport(this.objectMapper);
        this.linkPreviewSources = List.of(
                new DomesticEcommerceProductLinkPreviewSource(this.linkPreviewHttpClient, this.linkPreviewHtmlSupport),
                new WebsiteMetadataProductLinkPreviewSource(this.linkPreviewHttpClient, this.linkPreviewHtmlSupport)
        );
    }

    @Override
    public Long uploadMaterial(ProductMaterialDTO dto) {
        if (dto == null || dto.getProductName() == null || dto.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name must not be empty");
        }

        // P5.3: validate file paths for traversal and file types
        validateFilePaths(dto);

        ProductMaterial material = new ProductMaterial();
        material.setBrandId(dto.getBrandId());
        material.setBrandName(StringUtils.hasText(dto.getBrandName()) ? dto.getBrandName().trim() : null);
        material.setProjectId(dto.getProjectId());
        material.setProductName(dto.getProductName());
        material.setCategory(dto.getCategory());
        material.setProductSku(dto.getSku());
        material.setPrice(dto.getPrice());
        material.setDescription(dto.getDescription());
        material.setImages(dto.getImages());
        material.setVideos(dto.getVideos());
        material.setDocuments(dto.getDocuments());
        material.setUploader(resolveUploader(dto.getUploader()));
        material.setUploadTime(LocalDateTime.now());
        material.setStatus(1);

        this.save(material);
        log.info("Upload material success, ID: {}", material.getId());
        return material.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductMaterialFileUploadResponseDTO uploadMaterialFiles(Long materialId, List<MultipartFile> files) {
        if (materialId == null) {
            throw new IllegalArgumentException("Material ID must not be null");
        }

        ProductMaterial material = requireUploadableMaterial(materialId);

        ProductMaterialFileUploadResponseDTO response = new ProductMaterialFileUploadResponseDTO();
        response.setMaterialId(materialId);
        response.setImages(copyList(material.getImages()));
        response.setDocuments(copyList(material.getDocuments()));
        response.setVideos(copyList(material.getVideos()));

        if (files == null || files.isEmpty()) {
            response.setStatus("NO_FILES");
            response.setMessage("未选择文件，商品资料已保持不变。");
            return response;
        }
        if (files.size() > materialUploadMaxCount()) {
            throw new IllegalArgumentException("Single material upload supports at most "
                    + materialUploadMaxCount() + " files");
        }

        Path uploadRoot = materialUploadRoot();
        Path materialDir = uploadRoot.resolve(String.valueOf(materialId)).normalize();
        ensureWithinUploadRoots(materialDir, "material upload directory");
        try {
            Files.createDirectories(materialDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to prepare material upload directory", e);
        }

        long maxFileSizeBytes = materialUploadMaxBytes();
        List<String> images = copyList(material.getImages());
        List<String> documents = copyList(material.getDocuments());
        List<String> videos = copyList(material.getVideos());
        List<String> savedPaths = new ArrayList<>();
        Set<String> uploadedTypes = new LinkedHashSet<>();

        for (MultipartFile file : files) {
            ProductMaterialFileUploadResponseDTO.FileItem item = new ProductMaterialFileUploadResponseDTO.FileItem();
            item.setOriginalName(file == null ? "" : safeOriginalFilename(file.getOriginalFilename()));
            try {
                if (file == null || file.isEmpty()) {
                    throw new IllegalArgumentException("file is empty");
                }
                String originalName = safeOriginalFilename(file.getOriginalFilename());
                String extension = FileUtil.getFileExtension(originalName).toLowerCase(Locale.ROOT);
                SecurityUtil.rejectPathTraversal(originalName, "material file name");
                SecurityUtil.requireAllowedExtension(originalName, V1_UPLOAD_EXTENSIONS, "material file");
                SecurityUtil.requireFileSizeWithinLimit(file.getSize(), maxFileSizeBytes, "material file");

                String fileType = classifyUploadedFileType(extension);
                String storedFileName = UUID.randomUUID() + "-" + sanitizeFileName(originalName);
                Path target = materialDir.resolve(storedFileName).normalize();
                ensureWithinUploadRoots(target, "material upload file");
                Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

                String storedPath = toStoredPath(target);
                appendUnique("IMAGE".equals(fileType) ? images : documents, storedPath);
                savedPaths.add(storedPath);
                uploadedTypes.add(fileType);

                item.setStoredPath(storedPath);
                item.setFileType(fileType);
                item.setExtension(extension);
                item.setFileSize(file.getSize());
                item.setStatus("UPLOADED");
                item.setMessage("文件已上传，解析将在对应插件接入后执行。");
                response.getUploadedFiles().add(item);
                response.setUploadedCount(response.getUploadedCount() + 1);
                response.setTotalSize(response.getTotalSize() + file.getSize());
            } catch (Exception e) {
                item.setStatus("FAILED");
                item.setMessage(SecurityUtil.safeClientMessage(e.getMessage()));
                response.getFailedFiles().add(item);
                response.setFailedCount(response.getFailedCount() + 1);
            }
        }

        if (response.getUploadedCount() > 0) {
            material.setImages(images);
            material.setDocuments(documents);
            material.setVideos(videos);
            material.setOriginalFilePath(savedPaths.get(0));
            material.setFileType(uploadedTypes.size() == 1 ? uploadedTypes.iterator().next() : "MIXED");
            material.setFileSize(response.getTotalSize());
            material.setParseStatus(0);
            this.updateById(material);
            response.setImages(images);
            response.setDocuments(documents);
            response.setVideos(videos);
            response.setParseTasks(createPendingParseTasks(materialId, response.getUploadedFiles()));
        }

        if (response.getUploadedCount() > 0 && response.getFailedCount() == 0) {
            response.setStatus("UPLOADED");
            response.setMessage("文件已上传并保存到商品资料；OCR、PDF 和 ZIP 解析将在插件接入后执行。");
        } else if (response.getUploadedCount() > 0) {
            response.setStatus("PARTIAL_UPLOADED");
            response.setMessage("部分文件已上传，其余文件未识别；已上传文件可继续用于生成。");
        } else {
            response.setStatus("UPLOAD_FAILED");
            response.setMessage("文件未能上传。你仍可删除文件后只用商品信息继续生成，或重新选择文件。");
        }
        return response;
    }

    @Override
    public List<MaterialParseTaskDTO> listMaterialParseTasks(Long materialId) {
        requireUploadableMaterial(materialId);
        if (materialParseTaskService == null) {
            return List.of();
        }
        return materialParseTaskService.listByMaterialId(materialId);
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

        if (dto.getBrandId() != null) {
            existing.setBrandId(dto.getBrandId());
        }
        if (dto.getBrandName() != null) {
            existing.setBrandName(dto.getBrandName());
        }

        if (dto.getProductName() != null) {
            existing.setProductName(dto.getProductName());
        }
        if (dto.getProjectId() != null) {
            existing.setProjectId(dto.getProjectId());
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
        if (dto.getUploader() != null) {
            existing.setUploader(dto.getUploader());
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
    public ProductLinkPreviewDTO previewProductLink(String rawUrl) {
        URI normalizedUri = normalizePreviewUri(rawUrl);
        ensurePreviewTargetAllowed(normalizedUri);
        URI canonicalUri = canonicalizePreviewUri(normalizedUri);
        ProductLinkPreviewContext context = new ProductLinkPreviewContext(
                rawUrl,
                normalizedUri,
                canonicalUri,
                normalizedUri.getHost(),
                inferPlatform(normalizedUri.getHost())
        );

        ProductLinkPreviewSource source = linkPreviewSources.stream()
                .filter(candidate -> candidate.supports(context))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No product link preview source matched the normalized URL"));
        return source.preview(context);
    }

    @Override
    public String parseMaterialContent(Long id) {
        ProductMaterial material = getMaterialById(id);
        StringBuilder content = new StringBuilder();

        if (material.getImages() != null && !material.getImages().isEmpty()) {
            try {
                String ocrText = FileUtil.extractTextFromImages(material.getImages());
                if (ocrText != null && !ocrText.isEmpty()) {
                    content.append("[Image Content]\n").append(ocrText).append("\n\n");
                } else {
                    content.append("[Image Content]\n图片 OCR 当前未接入，已跳过图片文本提取。\n\n");
                }
            } catch (UnsupportedOperationException ignored) {
                content.append("[Image Content]\n图片 OCR 当前未接入，已跳过图片文本提取。\n\n");
            }
        }

        if (material.getDocuments() != null && !material.getDocuments().isEmpty()) {
            try {
                String docText = FileUtil.extractTextFromDocuments(material.getDocuments());
                if (docText != null && !docText.isEmpty()) {
                    content.append("[Document Content]\n").append(docText).append("\n\n");
                } else {
                    content.append("[Document Content]\n文档解析当前未接入，已跳过文档文本提取。\n\n");
                }
            } catch (UnsupportedOperationException ignored) {
                content.append("[Document Content]\n文档解析当前未接入，已跳过文档文本提取。\n\n");
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

    private URI normalizePreviewUri(String rawUrl) {
        if (!StringUtils.hasText(rawUrl)) {
            throw new IllegalArgumentException("商品链接不能为空");
        }
        String trimmed = rawUrl.trim();
        String candidate = trimmed.matches("(?i)^https?://.*$") ? trimmed : "https://" + trimmed;
        try {
            URI uri = new URI(candidate).normalize();
            String scheme = Optional.ofNullable(uri.getScheme()).orElse("").toLowerCase(Locale.ROOT);
            if (!"http".equals(scheme) && !"https".equals(scheme)) {
                throw new IllegalArgumentException("当前只支持 http 或 https 商品链接");
            }
            if (!StringUtils.hasText(uri.getHost())) {
                throw new IllegalArgumentException("商品链接缺少可识别域名");
            }
            if (uri.getUserInfo() != null) {
                throw new IllegalArgumentException("商品链接格式不受支持");
            }
            return uri;
        } catch (URISyntaxException error) {
            throw new IllegalArgumentException("商品链接格式不正确");
        }
    }

    private URI canonicalizePreviewUri(URI uri) {
        if (uri == null || !StringUtils.hasText(uri.getRawQuery())) {
            return uri;
        }
        List<String> filteredParams = new ArrayList<>();
        for (String pair : uri.getRawQuery().split("&")) {
            if (!StringUtils.hasText(pair)) {
                continue;
            }
            int separatorIndex = pair.indexOf('=');
            String rawKey = separatorIndex >= 0 ? pair.substring(0, separatorIndex) : pair;
            if (TRACKING_QUERY_KEYS.contains(rawKey.toLowerCase(Locale.ROOT))) {
                continue;
            }
            filteredParams.add(pair);
        }
        String cleanedQuery = filteredParams.isEmpty() ? null : String.join("&", filteredParams);
        try {
            return new URI(uri.getScheme(), uri.getRawAuthority(), uri.getRawPath(), cleanedQuery, null);
        } catch (URISyntaxException ignored) {
            return uri;
        }
    }

    private void ensurePreviewTargetAllowed(URI uri) {
        String host = Optional.ofNullable(uri.getHost()).orElse("").toLowerCase(Locale.ROOT);
        if (!StringUtils.hasText(host)) {
            throw new IllegalArgumentException("商品链接缺少可识别域名");
        }
        if (allowPrivatePreviewHosts()) {
            return;
        }
        if (PRIVATE_HOST_NAMES.contains(host) || host.endsWith(".local")) {
            throw new IllegalArgumentException("当前不支持读取本地或内网商品链接");
        }
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress address : addresses) {
                if (isPrivateAddress(address)) {
                    throw new IllegalArgumentException("当前不支持读取本地或内网商品链接");
                }
            }
        } catch (IOException error) {
            throw new IllegalArgumentException("商品链接域名暂时无法解析");
        }
    }

    private boolean allowPrivatePreviewHosts() {
        return Boolean.parseBoolean(property("shoppage.material-link-preview.allow-private-hosts", "false"));
    }

    private boolean isPrivateAddress(InetAddress address) {
        if (address == null) {
            return true;
        }
        if (address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()) {
            return true;
        }
        if (address instanceof Inet6Address inet6Address) {
            byte[] bytes = inet6Address.getAddress();
            return bytes.length > 0 && (bytes[0] & (byte) 0xfe) == (byte) 0xfc;
        }
        return false;
    }

    private String inferPlatform(String host) {
        String normalizedHost = Optional.ofNullable(host).orElse("").toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> entry : PLATFORM_HOST_MAP.entrySet()) {
            if (normalizedHost.equals(entry.getKey()) || normalizedHost.endsWith("." + entry.getKey())) {
                return entry.getValue();
            }
        }
        return "独立站";
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

    private List<String> copyList(List<String> values) {
        return values == null ? new ArrayList<>() : new ArrayList<>(values);
    }

    private void appendUnique(List<String> values, String value) {
        if (!values.contains(value)) {
            values.add(value);
        }
    }

    private String safeOriginalFilename(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "material-file";
        }
        String cleaned = originalFilename.trim().replace('\\', '/');
        int slashIndex = cleaned.lastIndexOf('/');
        return slashIndex >= 0 ? cleaned.substring(slashIndex + 1) : cleaned;
    }

    private String sanitizeFileName(String originalName) {
        String safe = safeOriginalFilename(originalName)
                .replaceAll("[^A-Za-z0-9._\\-\\u4e00-\\u9fa5]+", "-")
                .replaceAll("-{2,}", "-");
        if (!StringUtils.hasText(safe) || safe.equals(".") || safe.equals("..")) {
            return "material-file";
        }
        return safe.length() > 120 ? safe.substring(safe.length() - 120) : safe;
    }

    private String classifyUploadedFileType(String extension) {
        return V1_UPLOAD_IMAGE_EXTENSIONS.contains(extension) ? "IMAGE" : "DOCUMENT";
    }

    private Path materialUploadRoot() {
        String configuredRoot = property("shoppage.material-upload.root", "uploads/materials");
        Path root = LocalPathPolicy.requirePathWithinRoots(
                configuredRoot,
                materialUploadAllowedRoots(),
                "material upload root");
        return root.toAbsolutePath().normalize();
    }

    private void ensureWithinUploadRoots(Path path, String label) {
        if (!LocalPathPolicy.isWithinAllowedRoots(path, materialUploadAllowedRoots())) {
            throw new IllegalArgumentException(label + " is outside allowed upload roots");
        }
    }

    private List<Path> materialUploadAllowedRoots() {
        return LocalPathPolicy.parseAllowedRoots(
                property("security.allowed-upload-roots", ""),
                List.of("uploads"));
    }

    private long materialUploadMaxBytes() {
        String raw = property("shoppage.material-upload.max-file-size-bytes", "");
        if (StringUtils.hasText(raw)) {
            try {
                return Long.parseLong(raw.trim());
            } catch (NumberFormatException ignored) {
                log.warn("Invalid material upload max size {}, using default", raw);
            }
        }
        return DEFAULT_MATERIAL_UPLOAD_MAX_BYTES;
    }

    private int materialUploadMaxCount() {
        String raw = property("shoppage.material-upload.max-file-count", "");
        if (StringUtils.hasText(raw)) {
            try {
                return Integer.parseInt(raw.trim());
            } catch (NumberFormatException ignored) {
                log.warn("Invalid material upload max count {}, using default", raw);
            }
        }
        return DEFAULT_MATERIAL_UPLOAD_MAX_COUNT;
    }

    private ProductMaterial requireUploadableMaterial(Long materialId) {
        if (ownershipGuard != null) {
            return ownershipGuard.requireReadableProductMaterial(materialId, ownershipGuard.currentUser());
        }
        ProductMaterial material = this.getById(materialId);
        if (material == null) {
            throw new RuntimeException("Material not found, ID: " + materialId);
        }
        return material;
    }

    private List<MaterialParseTaskDTO> createPendingParseTasks(
            Long materialId,
            List<ProductMaterialFileUploadResponseDTO.FileItem> uploadedFiles) {
        if (materialParseTaskService == null || uploadedFiles == null || uploadedFiles.isEmpty()) {
            return List.of();
        }
        return materialParseTaskService.createPendingTasks(materialId, uploadedFiles);
    }

    private String resolveUploader(String fallback) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && StringUtils.hasText(authentication.getName())
                && !"anonymousUser".equals(authentication.getName())) {
            return authentication.getName().trim();
        }
        return fallback;
    }

    private String toStoredPath(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        Path cwd = Paths.get("").toAbsolutePath().normalize();
        if (normalized.startsWith(cwd)) {
            return cwd.relativize(normalized).toString().replace('\\', '/');
        }
        return normalized.toString();
    }

    private String property(String key, String defaultValue) {
        if (environment == null) {
            return defaultValue;
        }
        return environment.getProperty(key, defaultValue);
    }
}
