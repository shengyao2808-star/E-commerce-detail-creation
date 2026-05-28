package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.detail.ai.dto.ApplyGenerationResultsDTO;
import com.ecommerce.detail.ai.dto.ProductDetailDTO;
import com.ecommerce.detail.ai.entity.DetailGenerationResultLink;
import com.ecommerce.detail.ai.entity.GenerationResult;
import com.ecommerce.detail.ai.entity.ProductDetail;
import com.ecommerce.detail.ai.mapper.DetailGenerationResultLinkMapper;
import com.ecommerce.detail.ai.mapper.GenerationResultMapper;
import com.ecommerce.detail.ai.mapper.ProductDetailMapper;
import com.ecommerce.detail.ai.util.ExportUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductDetailServiceApplyGenerationResultsTest {

    @Test
    void applyGenerationResultsUsesExplicitIdsMergesImagesAndIsIdempotent() throws Exception {
        ProductDetail detail = new ProductDetail();
        detail.setId(100L);
        detail.setTitle("筛选结果应用");
        detail.setImages("[\"https://cdn.example/existing.png\",\"https://cdn.example/keep.png\"]");

        GenerationResult first = result(11L, "https://cdn.example/keep.png", 0);
        GenerationResult second = result(12L, "https://cdn.example/new.png", 0);

        RecordingProductDetailMapper productMapper = new RecordingProductDetailMapper(detail);
        RecordingGenerationResultMapper generationMapper = new RecordingGenerationResultMapper(Map.of(11L, first, 12L, second), List.of());
        RecordingDetailGenerationResultLinkMapper linkMapper = new RecordingDetailGenerationResultLinkMapper();
        ProductDetailServiceImpl service = service(productMapper, generationMapper, linkMapper);

        ApplyGenerationResultsDTO dto = new ApplyGenerationResultsDTO();
        dto.setGenerationResultIds(List.of(11L, 12L));

        int applied = service.applyGenerationResults(100L, dto);
        int appliedAgain = service.applyGenerationResults(100L, dto);

        assertEquals(2, applied);
        assertEquals(2, appliedAgain);
        assertEquals("[\"https://cdn.example/existing.png\",\"https://cdn.example/keep.png\",\"https://cdn.example/new.png\"]",
                productMapper.updatedDetail.getImages());
        assertEquals(2, linkMapper.insertCount);
        assertEquals(2, linkMapper.rows.size());
        assertTrue(linkMapper.rows.values().stream().anyMatch(link -> link.getGenerationResultId().equals(11L)));
        assertTrue(linkMapper.rows.values().stream().anyMatch(link -> link.getGenerationResultId().equals(12L)));
    }

    @Test
    void applyGenerationResultsFallsBackToSelectedRowsWhenIdsMissing() {
        ProductDetail detail = new ProductDetail();
        detail.setId(101L);
        detail.setTitle("默认选中应用");
        detail.setImages("[]");

        GenerationResult selected = result(21L, "https://cdn.example/selected.png", 1);
        GenerationResult ignored = result(22L, "https://cdn.example/ignored.png", 0);

        RecordingProductDetailMapper productMapper = new RecordingProductDetailMapper(detail);
        RecordingGenerationResultMapper generationMapper = new RecordingGenerationResultMapper(Map.of(21L, selected, 22L, ignored), List.of(selected));
        RecordingDetailGenerationResultLinkMapper linkMapper = new RecordingDetailGenerationResultLinkMapper();
        ProductDetailServiceImpl service = service(productMapper, generationMapper, linkMapper);

        ApplyGenerationResultsDTO dto = new ApplyGenerationResultsDTO();
        dto.setSelectedOnly(Boolean.TRUE);

        int applied = service.applyGenerationResults(101L, dto);

        assertEquals(1, applied);
        assertEquals("[\"https://cdn.example/selected.png\"]", productMapper.updatedDetail.getImages());
        assertEquals(1, linkMapper.insertCount);
    }

    @Test
    void applyGenerationResultsRejectsEmptySelection() {
        ProductDetail detail = new ProductDetail();
        detail.setId(102L);
        detail.setTitle("空选择校验");

        RecordingProductDetailMapper productMapper = new RecordingProductDetailMapper(detail);
        RecordingGenerationResultMapper generationMapper = new RecordingGenerationResultMapper(Map.of(), List.of());
        RecordingDetailGenerationResultLinkMapper linkMapper = new RecordingDetailGenerationResultLinkMapper();
        ProductDetailServiceImpl service = service(productMapper, generationMapper, linkMapper);

        assertThrows(IllegalArgumentException.class, () -> service.applyGenerationResults(102L, new ApplyGenerationResultsDTO()));
    }

    @Test
    void applyGenerationResultsRejectsBlankResultUrl() {
        ProductDetail detail = new ProductDetail();
        detail.setId(103L);
        detail.setTitle("空URL校验");

        GenerationResult invalid = result(31L, "   ", 1);

        RecordingProductDetailMapper productMapper = new RecordingProductDetailMapper(detail);
        RecordingGenerationResultMapper generationMapper = new RecordingGenerationResultMapper(Map.of(31L, invalid), List.of());
        RecordingDetailGenerationResultLinkMapper linkMapper = new RecordingDetailGenerationResultLinkMapper();
        ProductDetailServiceImpl service = service(productMapper, generationMapper, linkMapper);

        ApplyGenerationResultsDTO dto = new ApplyGenerationResultsDTO();
        dto.setGenerationResultIds(List.of(31L));

        assertThrows(IllegalArgumentException.class, () -> service.applyGenerationResults(103L, dto));
    }

    @Test
    void exportProductDetailParsesJsonMediaListsBeforeDelegating() {
        ProductDetail detail = new ProductDetail();
        detail.setId(104L);
        detail.setTitle("导出媒体解析");
        detail.setProductName("导出媒体解析");
        detail.setSku("SKU-104");
        detail.setCategory("分类");
        detail.setPrice(java.math.BigDecimal.TEN);
        detail.setDescription("描述");
        detail.setAiGeneratedContent("AI内容");
        detail.setImages("[\"https://cdn.example/image-1.png\",\"https://cdn.example/image-2.png\"]");
        detail.setVideos("[\"https://cdn.example/video-1.mp4\"]");
        detail.setDocuments("[\"https://cdn.example/doc-1.pdf\",\"https://cdn.example/doc-2.pdf\"]");

        RecordingProductDetailMapper productMapper = new RecordingProductDetailMapper(detail);
        RecordingExportUtil exportUtil = new RecordingExportUtil();
        ProductDetailServiceImpl service = service(productMapper, new RecordingGenerationResultMapper(Map.of(), List.of()), new RecordingDetailGenerationResultLinkMapper());
        ReflectionTestUtils.setField(service, "exportUtil", exportUtil);

        String path = service.exportProductDetail(104L, "txt");

        assertEquals("exports/recorded.txt", path);
        assertEquals(List.of("https://cdn.example/image-1.png", "https://cdn.example/image-2.png"), exportUtil.capturedDto.getImages());
        assertEquals(List.of("https://cdn.example/video-1.mp4"), exportUtil.capturedDto.getVideos());
        assertEquals(List.of("https://cdn.example/doc-1.pdf", "https://cdn.example/doc-2.pdf"), exportUtil.capturedDto.getDocuments());
    }

    private static ProductDetailServiceImpl service(RecordingProductDetailMapper productMapper,
                                                    RecordingGenerationResultMapper generationMapper,
                                                    RecordingDetailGenerationResultLinkMapper linkMapper) {
        ProductDetailServiceImpl service = new ProductDetailServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", productMapper.proxy());
        ReflectionTestUtils.setField(service, "generationResultMapper", generationMapper.proxy());
        ReflectionTestUtils.setField(service, "detailGenerationResultLinkMapper", linkMapper.proxy());
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        return service;
    }

    private static GenerationResult result(Long id, String resultUrl, Integer selected) {
        GenerationResult result = new GenerationResult();
        result.setId(id);
        result.setImageJobId(900L);
        result.setResultUrl(resultUrl);
        result.setSelected(selected);
        return result;
    }

    private static Object defaultValue(Class<?> returnType) {
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

    private static final class RecordingProductDetailMapper {
        private final ProductDetail storedDetail;
        private ProductDetail updatedDetail;
        private int updateCount;

        private RecordingProductDetailMapper(ProductDetail storedDetail) {
            this.storedDetail = storedDetail;
        }

        private ProductDetailMapper proxy() {
            return (ProductDetailMapper) Proxy.newProxyInstance(
                    ProductDetailMapper.class.getClassLoader(),
                    new Class<?>[]{ProductDetailMapper.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();
                        if ("selectById".equals(methodName)) {
                            Serializable id = (Serializable) args[0];
                            return storedDetail.getId().equals(id) ? storedDetail : null;
                        }
                        if ("updateById".equals(methodName)) {
                            updatedDetail = (ProductDetail) args[0];
                            updateCount++;
                            return 1;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }

    private static final class RecordingGenerationResultMapper {
        private final Map<Long, GenerationResult> byId;
        private final List<GenerationResult> selectedRows;

        private RecordingGenerationResultMapper(Map<Long, GenerationResult> byId, List<GenerationResult> selectedRows) {
            this.byId = new LinkedHashMap<>(byId);
            this.selectedRows = new ArrayList<>(selectedRows);
        }

        private GenerationResultMapper proxy() {
            return (GenerationResultMapper) Proxy.newProxyInstance(
                    GenerationResultMapper.class.getClassLoader(),
                    new Class<?>[]{GenerationResultMapper.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();
                        if ("selectById".equals(methodName)) {
                            return byId.get(args[0]);
                        }
                        if ("selectList".equals(methodName)) {
                            return new ArrayList<>(byId.values());
                        }
                        if ("selectPage".equals(methodName)) {
                            @SuppressWarnings("unchecked")
                            Page<GenerationResult> page = (Page<GenerationResult>) args[0];
                            page.setRecords(selectedRows);
                            page.setTotal(selectedRows.size());
                            return page;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }

    private static final class RecordingDetailGenerationResultLinkMapper {
        private final Map<String, DetailGenerationResultLink> rows = new LinkedHashMap<>();
        private int insertCount;
        private int updateCount;
        private long nextId = 1L;

        private DetailGenerationResultLinkMapper proxy() {
            return (DetailGenerationResultLinkMapper) Proxy.newProxyInstance(
                    DetailGenerationResultLinkMapper.class.getClassLoader(),
                    new Class<?>[]{DetailGenerationResultLinkMapper.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();
                        if ("selectList".equals(methodName)) {
                            return new ArrayList<>(rows.values());
                        }
                        if ("insert".equals(methodName)) {
                            DetailGenerationResultLink link = (DetailGenerationResultLink) args[0];
                            link.setId(nextId++);
                            rows.put(key(link.getProductDetailId(), link.getResultUrl()), link);
                            insertCount++;
                            return 1;
                        }
                        if ("updateById".equals(methodName)) {
                            DetailGenerationResultLink link = (DetailGenerationResultLink) args[0];
                            rows.put(key(link.getProductDetailId(), link.getResultUrl()), link);
                            updateCount++;
                            return 1;
                        }
                        if ("selectById".equals(methodName)) {
                            return rows.values().stream()
                                    .filter(link -> link.getId().equals(args[0]))
                                    .findFirst()
                                    .orElse(null);
                        }
                        return defaultValue(method.getReturnType());
                    });
        }

        private String key(Long detailId, String resultUrl) {
            return detailId + "|" + resultUrl;
        }
    }

    private static final class RecordingExportUtil extends ExportUtil {
        private ProductDetailDTO capturedDto;

        private RecordingExportUtil() {
            super(null);
        }

        @Override
        public String exportProductDetail(com.ecommerce.detail.ai.dto.ProductDetailDTO dto,
                                          com.ecommerce.detail.ai.entity.BrandTemplate template,
                                          String format) {
            capturedDto = dto;
            return "exports/recorded.txt";
        }
    }
}
