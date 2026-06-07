package com.ecommerce.detail.ai.service.impl;

import com.ecommerce.detail.ai.dto.ProductLinkPreviewDTO;
import com.ecommerce.detail.ai.dto.ProductMaterialDTO;
import com.ecommerce.detail.ai.dto.MaterialParseTaskDTO;
import com.ecommerce.detail.ai.dto.ProductMaterialFileUploadResponseDTO;
import com.ecommerce.detail.ai.entity.ProductMaterial;
import com.ecommerce.detail.ai.mapper.ProductMaterialMapper;
import com.ecommerce.detail.ai.service.MaterialParseTaskService;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductMaterialServiceImplTest {

    private final Path uploadRoot = Path.of("uploads", "test-material-upload-" + UUID.randomUUID());
    private HttpServer previewServer;

    @AfterEach
    void cleanup() throws IOException {
        SecurityContextHolder.clearContext();
        if (previewServer != null) {
            previewServer.stop(0);
            previewServer = null;
        }
        if (Files.exists(uploadRoot)) {
            try (var stream = Files.walk(uploadRoot)) {
                stream.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException ignored) {
                                // Best-effort cleanup for test artifacts.
                            }
                        });
            }
        }
    }

    @Test
    void uploadMaterialUsesAuthenticatedUserAsUploader() {
        RecordingProductMaterialMapper mapper = new RecordingProductMaterialMapper();
        ProductMaterialServiceImpl service = service(mapper);
        authenticate("alice", "ROLE_USER");

        ProductMaterialDTO dto = new ProductMaterialDTO();
        dto.setBrandId(1001L);
        dto.setBrandName("示例品牌A");
        dto.setProductName("智能手持风扇");
        dto.setCategory("小家电");
        dto.setUploader("商页工坊");

        Long id = service.uploadMaterial(dto);

        assertEquals(501L, id);
        assertNotNull(mapper.inserted);
        assertEquals(1001L, mapper.inserted.getBrandId());
        assertEquals("示例品牌A", mapper.inserted.getBrandName());
        assertEquals("alice", mapper.inserted.getUploader());
    }

    @Test
    void uploadMaterialFilesStoresSupportedFilesAndUpdatesMaterialLists() {
        RecordingProductMaterialMapper mapper = new RecordingProductMaterialMapper();
        ProductMaterial material = material(88L, "alice");
        mapper.rows.put(88L, material);
        RecordingMaterialParseTaskService parseTaskService = new RecordingMaterialParseTaskService();
        ProductMaterialServiceImpl service = service(mapper, parseTaskService);
        authenticate("alice", "ROLE_USER");

        List<MultipartFile> files = List.of(
                new MockMultipartFile("files", "主图.png", "image/png", new byte[]{1, 2, 3}),
                new MockMultipartFile("files", "说明书.pdf", "application/pdf", new byte[]{4, 5})
        );

        ProductMaterialFileUploadResponseDTO response = service.uploadMaterialFiles(88L, files);

        assertEquals("UPLOADED", response.getStatus());
        assertEquals(2, response.getUploadedCount());
        assertEquals(0, response.getFailedCount());
        assertEquals(1, response.getImages().size());
        assertEquals(1, response.getDocuments().size());
        assertEquals(1, mapper.updateCount);
        assertEquals("MIXED", mapper.updated.getFileType());
        assertEquals(5L, mapper.updated.getFileSize());
        assertEquals(2, parseTaskService.created.size());
        assertEquals(2, response.getParseTasks().size());
        assertEquals("主图.png", response.getParseTasks().get(0).getOriginalName());
        assertTrue(mapper.updated.getImages().get(0).startsWith("uploads/test-material-upload-"));
        assertTrue(mapper.updated.getDocuments().get(0).startsWith("uploads/test-material-upload-"));
        assertTrue(Files.exists(Path.of(mapper.updated.getImages().get(0))));
        assertTrue(Files.exists(Path.of(mapper.updated.getDocuments().get(0))));
    }

    @Test
    void uploadMaterialFilesReportsUnsupportedFormatsWithoutUpdatingMaterial() {
        RecordingProductMaterialMapper mapper = new RecordingProductMaterialMapper();
        mapper.rows.put(89L, material(89L, "alice"));
        ProductMaterialServiceImpl service = service(mapper);
        authenticate("alice", "ROLE_USER");

        ProductMaterialFileUploadResponseDTO response = service.uploadMaterialFiles(
                89L,
                List.of(new MockMultipartFile("files", "恶意脚本.exe", "application/octet-stream", new byte[]{1})));

        assertEquals("UPLOAD_FAILED", response.getStatus());
        assertEquals(0, response.getUploadedCount());
        assertEquals(1, response.getFailedCount());
        assertEquals(0, mapper.updateCount);
        assertTrue(response.getFailedFiles().get(0).getMessage().contains("unsupported file type"));
    }

    @Test
    void uploadMaterialFilesRejectsTooManyFilesBeforeWriting() {
        RecordingProductMaterialMapper mapper = new RecordingProductMaterialMapper();
        mapper.rows.put(90L, material(90L, "alice"));
        ProductMaterialServiceImpl service = service(mapper);
        authenticate("alice", "ROLE_USER");

        List<MultipartFile> files = java.util.stream.IntStream.range(0, 41)
                .mapToObj(index -> new MockMultipartFile(
                        "files",
                        "image-" + index + ".png",
                        "image/png",
                        new byte[]{1}))
                .map(MultipartFile.class::cast)
                .toList();

        IllegalArgumentException error = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.uploadMaterialFiles(90L, files));

        assertTrue(error.getMessage().contains("at most 40 files"));
        assertFalse(Files.exists(uploadRoot.resolve("materials").resolve("90")));
    }

    @Test
    void updateMaterialAppliesEditableMetadataFields() {
        RecordingProductMaterialMapper mapper = new RecordingProductMaterialMapper();
        ProductMaterial original = material(91L, "alice");
        original.setBrandId(7L);
        original.setBrandName("旧品牌");
        original.setUploader("old-uploader");
        original.setCategory("旧类目");
        original.setProductSku("OLD-SKU");
        original.setDescription("旧描述");
        original.setImages(List.of("old-image.png"));
        mapper.rows.put(91L, original);
        ProductMaterialServiceImpl service = service(mapper);
        authenticate("alice", "ROLE_USER");

        ProductMaterialDTO dto = new ProductMaterialDTO();
        dto.setBrandId(8L);
        dto.setBrandName("新品牌");
        dto.setProductName("升级商品");
        dto.setSku("NEW-SKU");
        dto.setCategory("新类目");
        dto.setUploader("new-uploader");
        dto.setPrice(java.math.BigDecimal.valueOf(99.9));
        dto.setDescription("新描述");
        dto.setImages(List.of("image-a.png", "image-b.png"));
        dto.setVideos(List.of("video-a.mp4"));
        dto.setDocuments(List.of("doc-a.pdf"));

        boolean updated = service.updateMaterial(91L, dto);

        assertTrue(updated);
        assertNotNull(mapper.updated);
        assertEquals(8L, mapper.updated.getBrandId());
        assertEquals("新品牌", mapper.updated.getBrandName());
        assertEquals("升级商品", mapper.updated.getProductName());
        assertEquals("NEW-SKU", mapper.updated.getProductSku());
        assertEquals("新类目", mapper.updated.getCategory());
        assertEquals("new-uploader", mapper.updated.getUploader());
        assertEquals(java.math.BigDecimal.valueOf(99.9), mapper.updated.getPrice());
        assertEquals("新描述", mapper.updated.getDescription());
        assertEquals(List.of("image-a.png", "image-b.png"), mapper.updated.getImages());
        assertEquals(List.of("video-a.mp4"), mapper.updated.getVideos());
        assertEquals(List.of("doc-a.pdf"), mapper.updated.getDocuments());
    }

    @Test
    void parseMaterialContentSkipsUnavailableImageAndDocumentExtraction() {
        RecordingProductMaterialMapper mapper = new RecordingProductMaterialMapper();
        ProductMaterial material = material(92L, "alice");
        material.setProductSku("SKU-92");
        material.setCategory("测试类目");
        material.setDescription("可直接继续生成的资料");
        material.setImages(List.of("uploads/test-material-upload/92/main.png"));
        material.setDocuments(List.of("uploads/test-material-upload/92/notes.bin"));
        mapper.rows.put(92L, material);
        ProductMaterialServiceImpl service = service(mapper);

        String content = service.parseMaterialContent(92L);

        assertTrue(content.contains("[Image Content]"));
        assertTrue(content.contains("图片 OCR 当前未接入"));
        assertTrue(content.contains("[Document Content]"));
        assertTrue(content.contains("文档解析当前未接入"));
        assertTrue(content.contains("[Product Info]"));
        assertTrue(content.contains("SKU-92"));
        assertTrue(content.contains("测试类目"));
    }

    @Test
    void previewProductLinkExtractsPlatformAndMetadataFromHtml() throws Exception {
        previewServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        previewServer.createContext("/product", exchange -> {
            byte[] body = """
                    <html>
                      <head>
                        <title>便携护眼台灯 - 京东</title>
                        <meta property="og:title" content="便携护眼台灯 学生宿舍款" />
                        <meta property="og:site_name" content="示例品牌" />
                        <script type="application/ld+json">
                        {
                          "@context":"https://schema.org",
                          "@type":"Product",
                          "name":"便携护眼台灯 学生宿舍款",
                          "category":"家居照明",
                          "brand":{"@type":"Brand","name":"示例品牌"}
                        }
                        </script>
                      </head>
                      <body>ok</body>
                    </html>
                    """.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        previewServer.start();

        ProductMaterialServiceImpl service = service(new RecordingProductMaterialMapper());
        ProductLinkPreviewDTO preview = service.previewProductLink("http://127.0.0.1:" + previewServer.getAddress().getPort() + "/product");

        assertEquals("独立站", preview.getPlatform());
        assertEquals("便携护眼台灯 学生宿舍款", preview.getProductName());
        assertEquals("家居照明", preview.getCategory());
        assertEquals("示例品牌", preview.getBrandName());
        assertEquals(200, preview.getHttpStatus());
        assertTrue(Boolean.TRUE.equals(preview.getFetched()));
        assertEquals("website-metadata", preview.getSource());
        assertFalse(Boolean.TRUE.equals(preview.getLoginRequired()));
    }

    @Test
    void previewProductLinkCanonicalizesTrackingQueryAndExplainsLoginGate() throws Exception {
        previewServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        previewServer.createContext("/product", exchange -> {
            byte[] body = """
                    <html>
                      <body>
                        <script>
                          window._config_ = {"action": "login"};
                        </script>
                        <a href="https://login.taobao.com/member/login.jhtml?redirectURL=foo">login</a>
                      </body>
                    </html>
                    """.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        previewServer.start();

        ProductMaterialServiceImpl service = service(new RecordingProductMaterialMapper());
        ProductLinkPreviewDTO preview = service.previewProductLink(
                "http://127.0.0.1:" + previewServer.getAddress().getPort() + "/product?id=123&spm=abc&utm_source=test&skuId=456");

        assertEquals("http://127.0.0.1:" + previewServer.getAddress().getPort() + "/product?id=123&skuId=456", preview.getResolvedUrl());
        assertEquals("已识别平台，但当前商品页需要登录后才能读取商品名称和类目。已先带入平台，请手动补充其余信息。", preview.getMessage());
        assertEquals(200, preview.getHttpStatus());
        assertTrue(Boolean.TRUE.equals(preview.getFetched()));
        assertTrue(Boolean.TRUE.equals(preview.getLoginRequired()));
        assertEquals("website-metadata", preview.getSource());
    }

    @Test
    void previewProductLinkFallsBackToBreadcrumbCategoryPath() throws Exception {
        previewServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        previewServer.createContext("/product", exchange -> {
            byte[] body = """
                    <html>
                      <head>
                        <title>客厅茶几</title>
                        <script type="application/ld+json">
                        {
                          "@context":"https://schema.org",
                          "@type":"BreadcrumbList",
                          "itemListElement":[
                            {"@type":"ListItem","position":1,"name":"住宅家具"},
                            {"@type":"ListItem","position":2,"name":"几类"},
                            {"@type":"ListItem","position":3,"name":"茶几"}
                          ]
                        }
                        </script>
                      </head>
                      <body>ok</body>
                    </html>
                    """.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        previewServer.start();

        ProductMaterialServiceImpl service = service(new RecordingProductMaterialMapper());
        ProductLinkPreviewDTO preview = service.previewProductLink("http://127.0.0.1:" + previewServer.getAddress().getPort() + "/product");

        assertEquals("住宅家具 / 几类 / 茶几", preview.getRawCategoryPath());
        assertEquals("茶几", preview.getCategory());
        assertEquals("website-metadata", preview.getSource());
    }

    @Test
    void previewProductLinkRejectsPrivateHostsWhenGuardEnabled() {
        ProductMaterialServiceImpl service = service(new RecordingProductMaterialMapper(), null, false);

        IllegalArgumentException error = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.previewProductLink("http://127.0.0.1:8080/test"));

        assertTrue(error.getMessage().contains("内网商品链接"));
    }

    private ProductMaterialServiceImpl service(RecordingProductMaterialMapper mapper) {
        return service(mapper, null);
    }

    private ProductMaterialServiceImpl service(
            RecordingProductMaterialMapper mapper,
            RecordingMaterialParseTaskService materialParseTaskService) {
        return service(mapper, materialParseTaskService, true);
    }

    private ProductMaterialServiceImpl service(
            RecordingProductMaterialMapper mapper,
            RecordingMaterialParseTaskService materialParseTaskService,
            boolean allowPrivatePreviewHosts) {
        ProductMaterialServiceImpl service = new ProductMaterialServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper.proxy());
        ReflectionTestUtils.setField(service, "environment", environment(allowPrivatePreviewHosts));
        if (materialParseTaskService != null) {
            ReflectionTestUtils.setField(service, "materialParseTaskService", materialParseTaskService.proxy());
        }
        return service;
    }

    private MockEnvironment environment(boolean allowPrivatePreviewHosts) {
        return new MockEnvironment()
                .withProperty("security.allowed-upload-roots", uploadRoot.toString())
                .withProperty("shoppage.material-upload.root", uploadRoot.resolve("materials").toString())
                .withProperty("shoppage.material-upload.max-file-size-bytes", "209715200")
                .withProperty("shoppage.material-upload.max-file-count", "40")
                .withProperty("shoppage.material-link-preview.allow-private-hosts", String.valueOf(allowPrivatePreviewHosts));
    }

    private ProductMaterial material(Long id, String uploader) {
        ProductMaterial material = new ProductMaterial();
        material.setId(id);
        material.setProductName("测试商品");
        material.setUploader(uploader);
        return material;
    }

    private void authenticate(String username, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        username,
                        "token",
                        List.of(new SimpleGrantedAuthority(role))));
    }

    private Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (boolean.class.equals(returnType)) {
            return false;
        }
        if (void.class.equals(returnType)) {
            return null;
        }
        return 0;
    }

    private class RecordingProductMaterialMapper {
        private final Map<Long, ProductMaterial> rows = new LinkedHashMap<>();
        private ProductMaterial inserted;
        private ProductMaterial updated;
        private int updateCount;

        private ProductMaterialMapper proxy() {
            return (ProductMaterialMapper) Proxy.newProxyInstance(
                    ProductMaterialMapper.class.getClassLoader(),
                    new Class<?>[]{ProductMaterialMapper.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();
                        if ("selectById".equals(methodName)) {
                            return rows.get(args[0]);
                        }
                        if ("insert".equals(methodName)) {
                            inserted = (ProductMaterial) args[0];
                            inserted.setId(501L);
                            rows.put(inserted.getId(), inserted);
                            return 1;
                        }
                        if ("updateById".equals(methodName)) {
                            updated = (ProductMaterial) args[0];
                            rows.put(updated.getId(), updated);
                            updateCount++;
                            return 1;
                        }
                        if ("deleteById".equals(methodName)) {
                            rows.remove((Serializable) args[0]);
                            return 1;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }

    private class RecordingMaterialParseTaskService {
        private final List<MaterialParseTaskDTO> created = new java.util.ArrayList<>();

        private MaterialParseTaskService proxy() {
            return (MaterialParseTaskService) Proxy.newProxyInstance(
                    MaterialParseTaskService.class.getClassLoader(),
                    new Class<?>[]{MaterialParseTaskService.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();
                        if ("createPendingTasks".equals(methodName)) {
                            @SuppressWarnings("unchecked")
                            List<ProductMaterialFileUploadResponseDTO.FileItem> files =
                                    (List<ProductMaterialFileUploadResponseDTO.FileItem>) args[1];
                            created.clear();
                            for (ProductMaterialFileUploadResponseDTO.FileItem file : files) {
                                MaterialParseTaskDTO task = new MaterialParseTaskDTO();
                                task.setMaterialId((Long) args[0]);
                                task.setOriginalName(file.getOriginalName());
                                task.setStoredPath(file.getStoredPath());
                                task.setStatus("PENDING");
                                task.setUserMessage("等待解析插件接入");
                                created.add(task);
                            }
                            return List.copyOf(created);
                        }
                        if ("listByMaterialId".equals(methodName)) {
                            return List.copyOf(created);
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }
}
