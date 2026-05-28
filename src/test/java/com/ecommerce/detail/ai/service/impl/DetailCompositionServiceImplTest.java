package com.ecommerce.detail.ai.service.impl;

import com.ecommerce.detail.ai.dto.DetailCompositionCreateDTO;
import com.ecommerce.detail.ai.dto.DetailCompositionDTO;
import com.ecommerce.detail.ai.dto.tool.ToolAdapterInfoDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeRequestDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeResponseDTO;
import com.ecommerce.detail.ai.entity.DetailComposition;
import com.ecommerce.detail.ai.entity.DetailCompositionResult;
import com.ecommerce.detail.ai.entity.ProductDetail;
import com.ecommerce.detail.ai.mapper.DetailCompositionMapper;
import com.ecommerce.detail.ai.mapper.DetailCompositionResultMapper;
import com.ecommerce.detail.ai.mapper.ProductDetailMapper;
import com.ecommerce.detail.ai.service.ToolAdapterService;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskExecutor;

import java.io.File;
import java.io.FileOutputStream;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DetailCompositionServiceImplTest {

    @Test
    void createCompositionSucceedsWhenToolReturnsRealPngPath() throws Exception {
        InMemoryState state = new InMemoryState();
        File input = createTempPngFile("exports/detail-compositions/input-1.png", 24, 18);
        File output = createTempPngFile("exports/detail-compositions/output-1.png", 48, 96);
        ProductDetail detail = sampleDetail(input.getAbsolutePath());
        state.productDetails.put(1L, detail);

        DetailCompositionServiceImpl service = buildService(state, configuredTool(output.getAbsolutePath()), command -> command.run());
        DetailCompositionCreateDTO dto = new DetailCompositionCreateDTO();
        dto.setProductDetailId(1L);
        dto.setTaskName("Compose detail");
        dto.setToolCode("imagemagick");
        dto.setModuleOrder(java.util.List.of("Hero", "Images"));
        dto.setDetailData(sampleDetailDto(input.getAbsolutePath()));

        Long id = service.createDetailComposition(dto);
        DetailCompositionDTO composition = service.getDetailCompositionById(id);

        assertEquals("SUCCEEDED", composition.getStatus());
        assertEquals(output.getAbsolutePath(), composition.getOutputPath());
        assertEquals("Compose detail", ((Map<?, ?>) composition.getInputData()).get("taskName"));
        assertEquals(48, composition.getOutputWidth());
        assertEquals(96, composition.getOutputHeight());
        assertEquals(1, state.results.size());
        DetailCompositionResult result = state.results.values().iterator().next();
        assertEquals(output.getAbsolutePath(), result.getOutputPath());
        assertEquals(Integer.valueOf(48), result.getImageWidth());
        assertEquals(Integer.valueOf(96), result.getImageHeight());
    }

    @Test
    void createCompositionCancelsWhenToolIsUnavailable() throws Exception {
        InMemoryState state = new InMemoryState();
        File input = createTempPngFile("exports/detail-compositions/input-2.png", 24, 18);
        state.productDetails.put(1L, sampleDetail(input.getAbsolutePath()));

        DetailCompositionServiceImpl service = buildService(state, unavailableTool(), command -> command.run());
        DetailCompositionCreateDTO dto = new DetailCompositionCreateDTO();
        dto.setProductDetailId(1L);
        dto.setToolCode("imagemagick");

        Long id = service.createDetailComposition(dto);
        DetailCompositionDTO composition = service.getDetailCompositionById(id);

        assertEquals("CANCELED", composition.getStatus());
        assertTrue(composition.getErrorMessage().contains("not configured"));
        assertTrue(state.results.isEmpty());
    }

    @Test
    void createCompositionFailsClosedWhenOutputPathIsMissing() throws Exception {
        InMemoryState state = new InMemoryState();
        File input = createTempPngFile("exports/detail-compositions/input-3.png", 24, 18);
        state.productDetails.put(1L, sampleDetail(input.getAbsolutePath()));

        DetailCompositionServiceImpl service = buildService(state, responseWithoutOutput(), command -> command.run());
        DetailCompositionCreateDTO dto = new DetailCompositionCreateDTO();
        dto.setProductDetailId(1L);
        dto.setToolCode("imagemagick");

        Long id = service.createDetailComposition(dto);
        DetailCompositionDTO composition = service.getDetailCompositionById(id);

        assertEquals("FAILED", composition.getStatus());
        assertTrue(composition.getErrorMessage().contains("output path"));
        assertTrue(state.results.isEmpty());
    }

    @Test
    void createCompositionFailsClosedWhenOutputFileIsMissing() throws Exception {
        InMemoryState state = new InMemoryState();
        File input = createTempPngFile("exports/detail-compositions/input-3b.png", 24, 18);
        state.productDetails.put(1L, sampleDetail(input.getAbsolutePath()));

        String missingOutputPath = Path.of("exports/detail-compositions/missing-output.png").toAbsolutePath().toString();
        DetailCompositionServiceImpl service = buildService(state, configuredTool(missingOutputPath), command -> command.run());
        DetailCompositionCreateDTO dto = new DetailCompositionCreateDTO();
        dto.setProductDetailId(1L);
        dto.setToolCode("imagemagick");

        Long id = service.createDetailComposition(dto);
        DetailCompositionDTO composition = service.getDetailCompositionById(id);

        assertEquals("FAILED", composition.getStatus());
        assertTrue(composition.getErrorMessage().contains("does not exist"));
        assertTrue(state.results.isEmpty());
    }

    @Test
    void resolveDownloadFileRejectsPathsOutsideAllowedRoots() throws Exception {
        InMemoryState state = new InMemoryState();
        File input = createTempPngFile("exports/detail-compositions/input-guard.png", 24, 18);
        state.productDetails.put(1L, sampleDetail(input.getAbsolutePath()));
        File output = createTempPngFile("exports/detail-compositions/output-guard.png", 48, 96);

        DetailCompositionServiceImpl service = buildService(state, configuredTool(output.getAbsolutePath()), command -> command.run());
        DetailCompositionCreateDTO dto = new DetailCompositionCreateDTO();
        dto.setProductDetailId(1L);
        dto.setToolCode("imagemagick");

        Long id = service.createDetailComposition(dto);

        DetailComposition job = state.jobs.get(id);
        job.setOutputPath(Path.of("C:\\temp\\escape.png").toString());
        state.jobs.put(id, job);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.resolveDownloadFile(id));
        assertTrue(ex.getMessage().contains("outside"));
    }

    @Test
    void createCompositionFailsClosedWhenResponseMetadataDoesNotMatchTheFile() throws Exception {
        InMemoryState state = new InMemoryState();
        File input = createTempPngFile("exports/detail-compositions/input-5.png", 24, 18);
        File output = createTempPngFile("exports/detail-compositions/output-5.png", 48, 96);
        state.productDetails.put(1L, sampleDetail(input.getAbsolutePath()));

        DetailCompositionServiceImpl service = buildService(state, proxy(ToolAdapterService.class, invocation -> {
            String method = invocation.getMethod().getName();
            if ("getTool".equals(method)) {
                return new ToolAdapterInfoDTO("imagemagick", "ImageMagick", "COMPOSITION_EXPORT", null, null, null, null, null, "compose", "/compose", java.util.List.of("compose"), true, "CONFIGURED");
            }
            if ("invoke".equals(method)) {
                return new ToolInvokeResponseDTO("imagemagick", "compose", 200, Map.of(
                        "outputPath", output.getAbsolutePath(),
                        "fileSize", 1L,
                        "width", 1,
                        "height", 1
                ), "{\"outputPath\":\"" + output.getAbsolutePath().replace("\\", "\\\\") + "\"}");
            }
            throw new UnsupportedOperationException(method);
        }), command -> command.run());

        DetailCompositionCreateDTO dto = new DetailCompositionCreateDTO();
        dto.setProductDetailId(1L);
        dto.setToolCode("imagemagick");

        Long id = service.createDetailComposition(dto);
        DetailCompositionDTO composition = service.getDetailCompositionById(id);

        assertEquals("FAILED", composition.getStatus());
        assertTrue(composition.getErrorMessage().contains("does not match"));
        assertTrue(state.results.isEmpty());
    }

    @Test
    void executeCompositionDoesNotReopenTerminalJob() throws Exception {
        InMemoryState state = new InMemoryState();
        File input = createTempPngFile("exports/detail-compositions/input-4.png", 24, 18);
        File output = createTempPngFile("exports/detail-compositions/output-4.png", 64, 128);
        state.productDetails.put(1L, sampleDetail(input.getAbsolutePath()));

        DetailCompositionServiceImpl service = buildService(state, configuredTool(output.getAbsolutePath()), command -> command.run());
        DetailCompositionCreateDTO dto = new DetailCompositionCreateDTO();
        dto.setProductDetailId(1L);
        dto.setToolCode("imagemagick");

        Long id = service.createDetailComposition(dto);
        assertEquals("SUCCEEDED", service.getDetailCompositionById(id).getStatus());

        invokePrivate(service, "executeComposition", new Class<?>[]{Long.class}, id);
        DetailCompositionDTO composition = service.getDetailCompositionById(id);

        assertEquals("SUCCEEDED", composition.getStatus());
        assertEquals(1, state.results.size());
    }

    private DetailCompositionServiceImpl buildService(
            InMemoryState state,
            ToolAdapterService toolAdapterService,
            TaskExecutor taskExecutor) throws Exception {
        DetailCompositionServiceImpl service = new DetailCompositionServiceImpl();
        setField(service, "productDetailMapper", proxy(ProductDetailMapper.class, invocation -> {
            if ("selectById".equals(invocation.getMethod().getName())) {
                return state.productDetails.get(invocation.getArguments()[0]);
            }
            throw new UnsupportedOperationException(invocation.getMethod().getName());
        }));
        setField(service, "detailCompositionResultMapper", proxy(DetailCompositionResultMapper.class, invocation -> {
            String method = invocation.getMethod().getName();
            if ("selectOne".equals(method)) {
                return state.results.values().stream().findFirst().orElse(null);
            }
            if ("insert".equals(method)) {
                DetailCompositionResult result = (DetailCompositionResult) invocation.getArguments()[0];
                long id = state.nextResultId.getAndIncrement();
                result.setId(id);
                state.results.put(id, cloneResult(result));
                return 1;
            }
            if ("updateById".equals(method)) {
                DetailCompositionResult result = (DetailCompositionResult) invocation.getArguments()[0];
                state.results.put(result.getId(), cloneResult(result));
                return 1;
            }
            if ("selectById".equals(method)) {
                return state.results.get(invocation.getArguments()[0]);
            }
            throw new UnsupportedOperationException(method);
        }));
        setField(service, "toolAdapterService", toolAdapterService);
        setField(service, "taskExecutor", taskExecutor);
        setField(service, "objectMapper", new com.fasterxml.jackson.databind.ObjectMapper());
        setField(service, DetailCompositionServiceImpl.class.getSuperclass(), "baseMapper", proxy(DetailCompositionMapper.class, invocation -> {
            String method = invocation.getMethod().getName();
            if ("insert".equals(method)) {
                DetailComposition job = (DetailComposition) invocation.getArguments()[0];
                long id = state.nextJobId.getAndIncrement();
                job.setId(id);
                state.jobs.put(id, cloneJob(job));
                return 1;
            }
            if ("updateById".equals(method)) {
                DetailComposition job = (DetailComposition) invocation.getArguments()[0];
                state.jobs.put(job.getId(), cloneJob(job));
                return 1;
            }
            if ("selectById".equals(method)) {
                return state.jobs.get(invocation.getArguments()[0]);
            }
            if ("selectOne".equals(method)) {
                return null;
            }
            throw new UnsupportedOperationException(method);
        }));
        return service;
    }

    private ProductDetail sampleDetail(String imagePath) {
        ProductDetail detail = new ProductDetail();
        detail.setId(1L);
        detail.setTitle("Sample Title");
        detail.setProductName("Sample Product");
        detail.setSellingPoints("one,two");
        detail.setSeoKeywords("alpha,beta");
        detail.setImages("[\"" + imagePath.replace("\\", "\\\\") + "\"]");
        detail.setDocuments("[\"doc.pdf\"]");
        return detail;
    }

    private com.ecommerce.detail.ai.dto.ProductDetailDTO sampleDetailDto(String imagePath) {
        com.ecommerce.detail.ai.dto.ProductDetailDTO dto = new com.ecommerce.detail.ai.dto.ProductDetailDTO();
        dto.setMaterialId(100L);
        dto.setBrandId(200L);
        dto.setTitle("Sample Title");
        dto.setProductName("Sample Product");
        dto.setSellingPoints(java.util.List.of("one", "two"));
        dto.setSeoKeywords(java.util.List.of("alpha", "beta"));
        dto.setModuleOrder(java.util.List.of("Hero", "Images"));
        dto.setImages(java.util.List.of(imagePath));
        return dto;
    }

    private ToolAdapterService configuredTool(String outputPath) {
        ToolAdapterInfoDTO tool = new ToolAdapterInfoDTO("imagemagick", "ImageMagick", "COMPOSITION_EXPORT", null, null, null, null, null, "compose", "/compose", java.util.List.of("compose"), true, "CONFIGURED");
        return proxy(ToolAdapterService.class, invocation -> {
            String method = invocation.getMethod().getName();
            if ("getTool".equals(method)) {
                return tool;
            }
            if ("invoke".equals(method)) {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("outputPath", outputPath);
                body.put("jobId", "external-001");
                return new ToolInvokeResponseDTO(
                        "imagemagick",
                        "compose",
                        200,
                        body,
                        "{\"outputPath\":\"" + outputPath.replace("\\", "\\\\") + "\"}");
            }
            throw new UnsupportedOperationException(method);
        });
    }

    private ToolAdapterService unavailableTool() {
        return proxy(ToolAdapterService.class, invocation -> {
            if ("getTool".equals(invocation.getMethod().getName())) {
                return new ToolAdapterInfoDTO("imagemagick", "ImageMagick", "COMPOSITION_EXPORT", null, null, null, null, null, "compose", "/compose", java.util.List.of("compose"), false, "NOT_CONFIGURED");
            }
            throw new UnsupportedOperationException(invocation.getMethod().getName());
        });
    }

    private ToolAdapterService responseWithoutOutput() {
        ToolAdapterInfoDTO tool = new ToolAdapterInfoDTO("imagemagick", "ImageMagick", "COMPOSITION_EXPORT", null, null, null, null, null, "compose", "/compose", java.util.List.of("compose"), true, "CONFIGURED");
        return proxy(ToolAdapterService.class, invocation -> {
            String method = invocation.getMethod().getName();
            if ("getTool".equals(method)) {
                return tool;
            }
            if ("invoke".equals(method)) {
                return new ToolInvokeResponseDTO("imagemagick", "compose", 200, Map.of("status", "ok"), "{\"status\":\"ok\"}");
            }
            throw new UnsupportedOperationException(method);
        });
    }

    private void invokePrivate(Object target, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        try {
            method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException() instanceof Exception exception ? exception : new RuntimeException(e.getTargetException());
        }
    }

    private File createTempPngFile(String relativePath, int width, int height) throws Exception {
        Path path = Path.of(relativePath).toAbsolutePath();
        Files.createDirectories(path.getParent());
        File file = path.toFile();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        javax.imageio.ImageIO.write(image, "png", file);
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

    private <T> T proxy(Class<T> type, SimpleInvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> handler.invoke(new Invocation(method, args))
        ));
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        setField(target, target.getClass(), fieldName, value);
    }

    private void setField(Object target, Class<?> type, String fieldName, Object value) throws Exception {
        Field field = findField(type, fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> type, String fieldName) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private static class InMemoryState {
        private final Map<Long, DetailComposition> jobs = new ConcurrentHashMap<>();
        private final Map<Long, DetailCompositionResult> results = new ConcurrentHashMap<>();
        private final AtomicLong nextJobId = new AtomicLong(1);
        private final AtomicLong nextResultId = new AtomicLong(1);
        private final Map<Long, ProductDetail> productDetails = new ConcurrentHashMap<>();
    }

    @FunctionalInterface
    private interface SimpleInvocationHandler {
        Object invoke(Invocation invocation) throws Throwable;
    }

    private static final class Invocation {
        private final Method method;
        private final Object[] arguments;

        private Invocation(Method method, Object[] arguments) {
            this.method = method;
            this.arguments = arguments;
        }

        private Method getMethod() {
            return method;
        }

        private Object[] getArguments() {
            return arguments;
        }
    }
}
