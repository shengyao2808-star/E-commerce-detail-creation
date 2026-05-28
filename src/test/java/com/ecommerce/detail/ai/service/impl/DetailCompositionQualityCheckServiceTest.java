package com.ecommerce.detail.ai.service.impl;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.DetailCompositionCreateDTO;
import com.ecommerce.detail.ai.dto.DetailCompositionDTO;
import com.ecommerce.detail.ai.dto.DetailCompositionQualityCheckDTO;
import com.ecommerce.detail.ai.dto.DetailDeliveryManifestDTO;
import com.ecommerce.detail.ai.dto.ProductDetailDTO;
import com.ecommerce.detail.ai.dto.tool.ToolAdapterInfoDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeRequestDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeResponseDTO;
import com.ecommerce.detail.ai.entity.DetailComposition;
import com.ecommerce.detail.ai.entity.DetailCompositionQualityCheck;
import com.ecommerce.detail.ai.entity.DetailCompositionResult;
import com.ecommerce.detail.ai.entity.DetailGenerationResultLink;
import com.ecommerce.detail.ai.entity.GenerationResult;
import com.ecommerce.detail.ai.entity.ProductDetail;
import com.ecommerce.detail.ai.mapper.DetailCompositionMapper;
import com.ecommerce.detail.ai.mapper.DetailCompositionQualityCheckMapper;
import com.ecommerce.detail.ai.mapper.DetailCompositionResultMapper;
import com.ecommerce.detail.ai.mapper.DetailGenerationResultLinkMapper;
import com.ecommerce.detail.ai.mapper.GenerationResultMapper;
import com.ecommerce.detail.ai.mapper.ProductDetailMapper;
import com.ecommerce.detail.ai.service.ToolAdapterService;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DetailCompositionQualityCheckServiceTest {

    @Test
    void createQualityCheckSucceedsWithRealScreenshotAndProducesDeliverableManifest() throws Exception {
        InMemoryState state = new InMemoryState();
        File input = createTempPngFile("exports/detail-compositions/qa-input-1.png", 24, 18);
        File output = createTempPngFile("exports/detail-compositions/qa-output-1.png", 640, 960);
        File screenshot = Path.of("exports/detail-compositions/quality-checks/qa-screenshot-1.png").toAbsolutePath().toFile();
        state.productDetails.put(1L, sampleDetail(input.getAbsolutePath()));

        DetailCompositionServiceImpl service = buildService(state, configuredTool(output.getAbsolutePath(), screenshot.getAbsolutePath()), Runnable::run);
        Long compositionId = createComposition(service, input.getAbsolutePath());
        seedAppliedGenerationResults(state, 1L);

        Long qualityCheckId = service.createQualityCheck(compositionId);
        DetailCompositionDTO composition = service.getDetailCompositionById(compositionId);
        PageResult<DetailCompositionQualityCheckDTO> checks = service.listQualityChecks(compositionId, 1, 10);
        DetailDeliveryManifestDTO manifest = service.getDeliveryManifest(compositionId);

        assertNotNull(qualityCheckId);
        assertEquals("SUCCEEDED", composition.getLatestQualityCheckStatus());
        assertEquals(0, composition.getLatestQualityCheckIssueCount());
        assertEquals(screenshot.getAbsolutePath(), composition.getLatestQualityCheckScreenshotPath());
        assertTrue(Boolean.TRUE.equals(composition.getDeliverable()));
        assertEquals(1, checks.getData().size());
        assertEquals("SUCCEEDED", checks.getData().get(0).getStatus());
        assertEquals(0, checks.getData().get(0).getIssueCount());
        assertEquals(1, manifest.getGenerationResults().size());
        assertEquals(Boolean.TRUE, manifest.getDeliverable());
        assertEquals("SUCCEEDED", manifest.getLatestQualityCheckStatus());
        assertEquals(composition.getOutputPath(), manifest.getOutputPath());
        assertTrue(manifest.getToolchain().contains("playwright"));
    }

    @Test
    void createQualityCheckCancelsWhenPlaywrightIsUnavailable() throws Exception {
        InMemoryState state = new InMemoryState();
        File input = createTempPngFile("exports/detail-compositions/qa-input-2.png", 24, 18);
        File output = createTempPngFile("exports/detail-compositions/qa-output-2.png", 640, 960);
        state.productDetails.put(1L, sampleDetail(input.getAbsolutePath()));

        DetailCompositionServiceImpl service = buildService(state, configuredToolWithoutPlaywright(output.getAbsolutePath()), Runnable::run);
        Long compositionId = createComposition(service, input.getAbsolutePath());

        Long qualityCheckId = service.createQualityCheck(compositionId);
        DetailCompositionDTO composition = service.getDetailCompositionById(compositionId);

        assertNotNull(qualityCheckId);
        assertEquals("CANCELED", composition.getLatestQualityCheckStatus());
        assertFalse(Boolean.TRUE.equals(composition.getDeliverable()));
        assertTrue(composition.getLatestQualityCheckScreenshotPath() == null || composition.getLatestQualityCheckScreenshotPath().isBlank());
    }

    @Test
    void createQualityCheckFailsClosedWhenAdapterResponseIsMalformed() throws Exception {
        InMemoryState state = new InMemoryState();
        File input = createTempPngFile("exports/detail-compositions/qa-input-3.png", 24, 18);
        File output = createTempPngFile("exports/detail-compositions/qa-output-3.png", 640, 960);
        state.productDetails.put(1L, sampleDetail(input.getAbsolutePath()));

        DetailCompositionServiceImpl service = buildService(state, malformedPlaywright(output.getAbsolutePath()), Runnable::run);
        Long compositionId = createComposition(service, input.getAbsolutePath());

        Long qualityCheckId = service.createQualityCheck(compositionId);
        DetailCompositionDTO composition = service.getDetailCompositionById(compositionId);

        assertNotNull(qualityCheckId);
        assertEquals("FAILED", composition.getLatestQualityCheckStatus());
        assertFalse(Boolean.TRUE.equals(composition.getDeliverable()));
        assertTrue(composition.getLatestQualityCheckIssueCount() == null || composition.getLatestQualityCheckIssueCount() >= 0);
    }

    @Test
    void getDeliveryManifestUsesPersistedRowsOnly() throws Exception {
        InMemoryState state = new InMemoryState();
        File input = createTempPngFile("exports/detail-compositions/qa-input-4.png", 24, 18);
        File output = createTempPngFile("exports/detail-compositions/qa-output-4.png", 800, 1200);
        File screenshot = Path.of("exports/detail-compositions/quality-checks/qa-screenshot-4.png").toAbsolutePath().toFile();
        state.productDetails.put(1L, sampleDetail(input.getAbsolutePath()));

        DetailCompositionServiceImpl service = buildService(state, configuredTool(output.getAbsolutePath(), screenshot.getAbsolutePath()), Runnable::run);
        Long compositionId = createComposition(service, input.getAbsolutePath());
        seedAppliedGenerationResults(state, 1L);
        service.createQualityCheck(compositionId);

        DetailDeliveryManifestDTO manifest = service.getDeliveryManifest(compositionId);

        assertEquals(1L, manifest.getDetailCompositionId());
        assertEquals(1L, manifest.getProductDetailId());
        assertEquals(service.getDetailCompositionById(compositionId).getOutputPath(), manifest.getOutputPath());
        assertEquals(1, manifest.getGenerationResults().size());
        assertTrue(manifest.getGenerationResults().get(0).containsKey("generationResultId"));
        assertTrue(manifest.getGenerationResults().get(0).containsKey("resultUrl"));
        assertEquals("SUCCEEDED", manifest.getLatestQualityCheckStatus());
    }

    private Long createComposition(DetailCompositionServiceImpl service, String inputPath) {
        DetailCompositionCreateDTO dto = new DetailCompositionCreateDTO();
        dto.setProductDetailId(1L);
        dto.setToolCode("imagemagick");
        dto.setTaskName("detail composition");
        dto.setModuleOrder(List.of("Hero", "Images"));
        dto.setDetailData(sampleDetailDto(inputPath));
        return service.createDetailComposition(dto);
    }

    private void seedAppliedGenerationResults(InMemoryState state, Long productDetailId) {
        GenerationResult result = new GenerationResult();
        result.setId(1001L);
        result.setImageJobId(5001L);
        result.setResultUrl("https://cdn.example.com/detail-1.png");
        result.setThumbnailUrl("https://cdn.example.com/detail-1-thumb.png");
        result.setPrompt("prompt-1");
        result.setParamsJson("{\"ratio\":\"1:1\"}");
        result.setComplianceStatus("APPROVED");
        result.setSelected(1);
        state.generationResults.put(result.getId(), result);

        DetailGenerationResultLink link = new DetailGenerationResultLink();
        link.setId(1L);
        link.setProductDetailId(productDetailId);
        link.setGenerationResultId(result.getId());
        link.setResultUrl(result.getResultUrl());
        state.links.put(link.getGenerationResultId(), link);
    }

    private DetailCompositionServiceImpl buildService(InMemoryState state, ToolAdapterService toolAdapterService, TaskExecutor taskExecutor) throws Exception {
        DetailCompositionServiceImpl service = new DetailCompositionServiceImpl();
        ReflectionTestUtils.setField(service, "productDetailMapper", proxy(ProductDetailMapper.class, invocation -> {
            if ("selectById".equals(invocation.getMethod().getName())) {
                Serializable id = (Serializable) invocation.getArguments()[0];
                return state.productDetails.get(id);
            }
            return defaultValue(invocation.getMethod().getReturnType());
        }));
        ReflectionTestUtils.setField(service, "detailCompositionResultMapper", proxy(DetailCompositionResultMapper.class, invocation -> {
            String method = invocation.getMethod().getName();
            if ("selectOne".equals(method)) {
                return state.compositionResults.values().stream().findFirst().orElse(null);
            }
            if ("insert".equals(method)) {
                DetailCompositionResult result = (DetailCompositionResult) invocation.getArguments()[0];
                result.setId(state.resultId.incrementAndGet());
                state.compositionResults.put(result.getId(), cloneResult(result));
                return 1;
            }
            if ("updateById".equals(method)) {
                DetailCompositionResult result = (DetailCompositionResult) invocation.getArguments()[0];
                state.compositionResults.put(result.getId(), cloneResult(result));
                return 1;
            }
            if ("selectById".equals(method)) {
                return state.compositionResults.get(invocation.getArguments()[0]);
            }
            return defaultValue(invocation.getMethod().getReturnType());
        }));
        ReflectionTestUtils.setField(service, "detailCompositionQualityCheckMapper", proxy(DetailCompositionQualityCheckMapper.class, invocation -> {
            String method = invocation.getMethod().getName();
            if ("selectOne".equals(method)) {
                return state.qualityChecks.values().stream()
                        .sorted((left, right) -> right.getCreateTime().compareTo(left.getCreateTime()))
                        .findFirst()
                        .orElse(null);
            }
            if ("selectList".equals(method)) {
                return new ArrayList<>(state.qualityChecks.values());
            }
            if ("insert".equals(method)) {
                DetailCompositionQualityCheck check = (DetailCompositionQualityCheck) invocation.getArguments()[0];
                check.setId(state.qualityCheckId.incrementAndGet());
                state.qualityChecks.put(check.getId(), cloneCheck(check));
                return 1;
            }
            if ("updateById".equals(method)) {
                DetailCompositionQualityCheck check = (DetailCompositionQualityCheck) invocation.getArguments()[0];
                state.qualityChecks.put(check.getId(), cloneCheck(check));
                return 1;
            }
            if ("selectById".equals(method)) {
                return state.qualityChecks.get(invocation.getArguments()[0]);
            }
            return defaultValue(invocation.getMethod().getReturnType());
        }));
        ReflectionTestUtils.setField(service, "detailGenerationResultLinkMapper", proxy(DetailGenerationResultLinkMapper.class, invocation -> {
            String method = invocation.getMethod().getName();
            if ("selectList".equals(method)) {
                return new ArrayList<>(state.links.values());
            }
            if ("selectOne".equals(method)) {
                return state.links.values().stream().findFirst().orElse(null);
            }
            if ("insert".equals(method)) {
                DetailGenerationResultLink link = (DetailGenerationResultLink) invocation.getArguments()[0];
                state.links.put(link.getGenerationResultId(), cloneLink(link));
                return 1;
            }
            if ("updateById".equals(method)) {
                DetailGenerationResultLink link = (DetailGenerationResultLink) invocation.getArguments()[0];
                state.links.put(link.getGenerationResultId(), cloneLink(link));
                return 1;
            }
            return defaultValue(invocation.getMethod().getReturnType());
        }));
        ReflectionTestUtils.setField(service, "generationResultMapper", proxy(GenerationResultMapper.class, invocation -> {
            if ("selectById".equals(invocation.getMethod().getName())) {
                Serializable id = (Serializable) invocation.getArguments()[0];
                return state.generationResults.get(id);
            }
            if ("selectList".equals(invocation.getMethod().getName())) {
                return new ArrayList<>(state.generationResults.values());
            }
            return defaultValue(invocation.getMethod().getReturnType());
        }));
        ReflectionTestUtils.setField(service, "toolAdapterService", toolAdapterService);
        ReflectionTestUtils.setField(service, "taskExecutor", taskExecutor);
        ReflectionTestUtils.setField(service, "objectMapper", new com.fasterxml.jackson.databind.ObjectMapper());
        ReflectionTestUtils.setField(service, "baseMapper", proxy(DetailCompositionMapper.class, invocation -> {
            String method = invocation.getMethod().getName();
            if ("insert".equals(method)) {
                DetailComposition job = (DetailComposition) invocation.getArguments()[0];
                job.setId(state.jobId.incrementAndGet());
                state.compositions.put(job.getId(), cloneJob(job));
                return 1;
            }
            if ("updateById".equals(method)) {
                DetailComposition job = (DetailComposition) invocation.getArguments()[0];
                state.compositions.put(job.getId(), cloneJob(job));
                return 1;
            }
            if ("selectById".equals(method)) {
                return state.compositions.get(invocation.getArguments()[0]);
            }
            if ("selectOne".equals(method)) {
                return state.compositions.values().stream().findFirst().orElse(null);
            }
            if ("selectList".equals(method)) {
                return new ArrayList<>(state.compositions.values());
            }
            return defaultValue(invocation.getMethod().getReturnType());
        }));
        return service;
    }

    private ToolAdapterService configuredTool(String outputPath, String screenshotPath) {
        return proxy(ToolAdapterService.class, invocation -> {
            String method = invocation.getMethod().getName();
            if ("getTool".equals(method)) {
                String code = String.valueOf(invocation.getArguments()[0]);
                if ("imagemagick".equals(code)) {
                    return new ToolAdapterInfoDTO("imagemagick", "ImageMagick", "COMPOSITION_EXPORT", null, null, null, null, null, "compose", "/compose", List.of("compose", "resize", "stitch", "convert"), true, "CONFIGURED");
                }
                if ("playwright".equals(code)) {
                    return new ToolAdapterInfoDTO("playwright", "Playwright", "VISUAL_QA", null, null, null, null, null, "verify-page", "/verify", List.of("verify-page", "screenshot", "layout-check"), true, "CONFIGURED");
                }
                return null;
            }
            if ("invoke".equals(method)) {
                ToolInvokeRequestDTO request = (ToolInvokeRequestDTO) invocation.getArguments()[1];
                Map<String, Object> payload = request.getPayload();
                String code = String.valueOf(invocation.getArguments()[0]);
                if ("imagemagick".equals(code)) {
                    String requestedOutputPath = String.valueOf(payload.get("outputPath"));
                    if (requestedOutputPath != null) {
                        ensurePng(requestedOutputPath, 640, 960);
                    }
                    Map<String, Object> body = new LinkedHashMap<>();
                    body.put("outputPath", requestedOutputPath);
                    body.put("fileSize", new File(requestedOutputPath).length());
                    body.put("width", 640);
                    body.put("height", 960);
                    body.put("jobId", "compose-1");
                    return new ToolInvokeResponseDTO("imagemagick", request.getOperation(), 200, body, "{\"status\":\"ok\"}");
                }
                String requestedScreenshotPath = String.valueOf(payload.get("screenshotPath"));
                if (requestedScreenshotPath != null) {
                    ensurePng(requestedScreenshotPath, 640, 960);
                }
                ensurePng(screenshotPath, 640, 960);
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("screenshotPath", screenshotPath);
                body.put("issueCount", 0);
                body.put("issues", List.of());
                body.put("width", 640);
                body.put("height", 960);
                body.put("fileSize", new File(screenshotPath).length());
                return new ToolInvokeResponseDTO("playwright", "verify-page", 200, body, "{\"status\":\"ok\"}");
            }
            return defaultValue(invocation.getMethod().getReturnType());
        });
    }

    private ToolAdapterService configuredToolWithoutPlaywright(String outputPath) {
        return proxy(ToolAdapterService.class, invocation -> {
            String method = invocation.getMethod().getName();
            if ("getTool".equals(method)) {
                String code = String.valueOf(invocation.getArguments()[0]);
                if ("imagemagick".equals(code)) {
                    return new ToolAdapterInfoDTO("imagemagick", "ImageMagick", "COMPOSITION_EXPORT", null, null, null, null, null, "compose", "/compose", List.of("compose", "resize", "stitch", "convert"), true, "CONFIGURED");
                }
                if ("playwright".equals(code)) {
                    return new ToolAdapterInfoDTO("playwright", "Playwright", "VISUAL_QA", null, null, null, null, null, "verify-page", "/verify", List.of("verify-page", "screenshot", "layout-check"), false, "NOT_CONFIGURED");
                }
            }
            if ("invoke".equals(method)) {
                ToolInvokeRequestDTO request = (ToolInvokeRequestDTO) invocation.getArguments()[1];
                Map<String, Object> payload = request.getPayload();
                String code = String.valueOf(invocation.getArguments()[0]);
                if ("imagemagick".equals(code)) {
                    String requestedOutputPath = String.valueOf(payload.get("outputPath"));
                    if (requestedOutputPath != null) {
                        ensurePng(requestedOutputPath, 640, 960);
                    }
                    Map<String, Object> body = new LinkedHashMap<>();
                    body.put("outputPath", requestedOutputPath);
                    body.put("fileSize", new File(requestedOutputPath).length());
                    body.put("width", 640);
                    body.put("height", 960);
                    body.put("jobId", "compose-1");
                    return new ToolInvokeResponseDTO("imagemagick", request.getOperation(), 200, body, "{\"status\":\"ok\"}");
                }
            }
            return defaultValue(invocation.getMethod().getReturnType());
        });
    }

    private ToolAdapterService malformedPlaywright(String outputPath) {
        return proxy(ToolAdapterService.class, invocation -> {
            String method = invocation.getMethod().getName();
            if ("getTool".equals(method)) {
                String code = String.valueOf(invocation.getArguments()[0]);
                if ("imagemagick".equals(code)) {
                    return new ToolAdapterInfoDTO("imagemagick", "ImageMagick", "COMPOSITION_EXPORT", null, null, null, null, null, "compose", "/compose", List.of("compose", "resize", "stitch", "convert"), true, "CONFIGURED");
                }
                if ("playwright".equals(code)) {
                    return new ToolAdapterInfoDTO("playwright", "Playwright", "VISUAL_QA", null, null, null, null, null, "verify-page", "/verify", List.of("verify-page", "screenshot", "layout-check"), true, "CONFIGURED");
                }
                return null;
            }
            if ("invoke".equals(method)) {
                ToolInvokeRequestDTO request = (ToolInvokeRequestDTO) invocation.getArguments()[1];
                Map<String, Object> payload = request.getPayload();
                String code = String.valueOf(invocation.getArguments()[0]);
                if ("imagemagick".equals(code)) {
                    String requestedOutputPath = String.valueOf(payload.get("outputPath"));
                    if (requestedOutputPath != null) {
                        ensurePng(requestedOutputPath, 640, 960);
                    }
                    Map<String, Object> body = new LinkedHashMap<>();
                    body.put("outputPath", requestedOutputPath);
                    body.put("fileSize", new File(requestedOutputPath).length());
                    body.put("width", 640);
                    body.put("height", 960);
                    body.put("jobId", "compose-1");
                    return new ToolInvokeResponseDTO("imagemagick", request.getOperation(), 200, body, "{\"status\":\"ok\"}");
                }
                return new ToolInvokeResponseDTO("playwright", "verify-page", 200, Map.of("status", "ok"), "{\"status\":\"ok\"}");
            }
            return defaultValue(invocation.getMethod().getReturnType());
        });
    }

    private ProductDetail sampleDetail(String imagePath) {
        ProductDetail detail = new ProductDetail();
        detail.setId(1L);
        detail.setMaterialId(10L);
        detail.setTitle("Sample title");
        detail.setProductName("Sample product");
        detail.setSellingPoints("[\"one\",\"two\"]");
        detail.setSeoKeywords("[\"alpha\",\"beta\"]");
        detail.setImages("[\"" + imagePath.replace("\\", "\\\\") + "\"]");
        detail.setModuleOrder("[\"Hero\",\"Images\"]");
        return detail;
    }

    private ProductDetailDTO sampleDetailDto(String imagePath) {
        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setMaterialId(10L);
        dto.setBrandId(20L);
        dto.setTitle("Sample title");
        dto.setProductName("Sample product");
        dto.setSellingPoints(List.of("one", "two"));
        dto.setSeoKeywords(List.of("alpha", "beta"));
        dto.setModuleOrder(List.of("Hero", "Images"));
        dto.setImages(List.of(imagePath));
        return dto;
    }

    private void ensurePng(String path, int width, int height) throws Exception {
        Path filePath = Path.of(path).toAbsolutePath();
        Files.createDirectories(filePath.getParent());
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        ImageIO.write(image, "png", filePath.toFile());
    }

    private File createTempPngFile(String relativePath, int width, int height) throws Exception {
        Path path = Path.of(relativePath).toAbsolutePath();
        Files.createDirectories(path.getParent());
        File file = path.toFile();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        ImageIO.write(image, "png", file);
        file.deleteOnExit();
        return file;
    }

    private DetailComposition cloneJob(DetailComposition source) {
        DetailComposition job = new DetailComposition();
        job.setId(source.getId());
        job.setProductDetailId(source.getProductDetailId());
        job.setTaskName(source.getTaskName());
        job.setToolCode(source.getToolCode());
        job.setInputJson(source.getInputJson());
        job.setStatus(source.getStatus());
        job.setProgress(source.getProgress());
        job.setExternalJobId(source.getExternalJobId());
        job.setOutputPath(source.getOutputPath());
        job.setErrorMessage(source.getErrorMessage());
        job.setCreateTime(source.getCreateTime());
        job.setUpdateTime(source.getUpdateTime());
        return job;
    }

    private DetailCompositionResult cloneResult(DetailCompositionResult source) {
        DetailCompositionResult result = new DetailCompositionResult();
        result.setId(source.getId());
        result.setDetailCompositionId(source.getDetailCompositionId());
        result.setOutputPath(source.getOutputPath());
        result.setFileName(source.getFileName());
        result.setFileSize(source.getFileSize());
        result.setImageWidth(source.getImageWidth());
        result.setImageHeight(source.getImageHeight());
        result.setMimeType(source.getMimeType());
        result.setCreateTime(source.getCreateTime());
        result.setUpdateTime(source.getUpdateTime());
        return result;
    }

    private DetailCompositionQualityCheck cloneCheck(DetailCompositionQualityCheck source) {
        DetailCompositionQualityCheck check = new DetailCompositionQualityCheck();
        check.setId(source.getId());
        check.setDetailCompositionId(source.getDetailCompositionId());
        check.setToolCode(source.getToolCode());
        check.setStatus(source.getStatus());
        check.setIssueCount(source.getIssueCount());
        check.setIssuesJson(source.getIssuesJson());
        check.setScreenshotPath(source.getScreenshotPath());
        check.setErrorMessage(source.getErrorMessage());
        check.setCheckTime(source.getCheckTime());
        check.setCreateTime(source.getCreateTime());
        check.setUpdateTime(source.getUpdateTime());
        return check;
    }

    private DetailGenerationResultLink cloneLink(DetailGenerationResultLink source) {
        DetailGenerationResultLink link = new DetailGenerationResultLink();
        link.setId(source.getId());
        link.setProductDetailId(source.getProductDetailId());
        link.setGenerationResultId(source.getGenerationResultId());
        link.setResultUrl(source.getResultUrl());
        link.setCreateTime(source.getCreateTime());
        link.setUpdateTime(source.getUpdateTime());
        return link;
    }

    private <T> T proxy(Class<T> type, SimpleInvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> handler.invoke(new Invocation(method, args))
        ));
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

    @FunctionalInterface
    private interface SimpleInvocationHandler {
        Object invoke(Invocation invocation) throws Throwable;
    }

    private static final class Invocation {
        private final java.lang.reflect.Method method;
        private final Object[] arguments;

        private Invocation(java.lang.reflect.Method method, Object[] arguments) {
            this.method = method;
            this.arguments = arguments;
        }

        private java.lang.reflect.Method getMethod() {
            return method;
        }

        private Object[] getArguments() {
            return arguments;
        }
    }

    private static class InMemoryState {
        private final Map<Long, ProductDetail> productDetails = new LinkedHashMap<>();
        private final Map<Long, DetailComposition> compositions = new LinkedHashMap<>();
        private final Map<Long, DetailCompositionResult> compositionResults = new LinkedHashMap<>();
        private final Map<Long, DetailCompositionQualityCheck> qualityChecks = new LinkedHashMap<>();
        private final Map<Long, GenerationResult> generationResults = new LinkedHashMap<>();
        private final Map<Long, DetailGenerationResultLink> links = new LinkedHashMap<>();
        private final AtomicLong jobId = new AtomicLong(0);
        private final AtomicLong resultId = new AtomicLong(0);
        private final AtomicLong qualityCheckId = new AtomicLong(0);
    }
}
