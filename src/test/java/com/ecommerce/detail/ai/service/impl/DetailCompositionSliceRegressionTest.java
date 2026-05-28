package com.ecommerce.detail.ai.service.impl;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.DetailCompositionCreateDTO;
import com.ecommerce.detail.ai.dto.DetailCompositionDTO;
import com.ecommerce.detail.ai.dto.DetailCompositionListQuery;
import com.ecommerce.detail.ai.dto.tool.ToolAdapterInfoDTO;
import com.ecommerce.detail.ai.entity.DetailComposition;
import com.ecommerce.detail.ai.entity.ProductDetail;
import com.ecommerce.detail.ai.mapper.DetailCompositionMapper;
import com.ecommerce.detail.ai.mapper.DetailCompositionResultMapper;
import com.ecommerce.detail.ai.mapper.ProductDetailMapper;
import com.ecommerce.detail.ai.service.ToolAdapterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DetailCompositionSliceRegressionTest {

    @Test
    void createPersistsRealCanceledRowWhenToolIsUnavailable() {
        File input = createTempPngFile("exports/detail-compositions/regression-input.png", 16, 24);
        RecordingDetailCompositionMapper compositionMapper = new RecordingDetailCompositionMapper();
        DetailCompositionServiceImpl service = service(compositionMapper, unavailableToolService(), input.getAbsolutePath());

        DetailCompositionCreateDTO dto = new DetailCompositionCreateDTO();
        dto.setProductDetailId(88L);
        dto.setToolCode("imagemagick");

        Long id = service.createDetailComposition(dto);

        assertEquals(401L, id);
        assertEquals("CANCELED", compositionMapper.updatedComposition.getStatus());
        assertTrue(compositionMapper.updatedComposition.getErrorMessage().contains("not configured"));
    }

    @Test
    void getAndListExposePersistedInputData() {
        DetailComposition stored = new DetailComposition();
        stored.setId(701L);
        stored.setProductDetailId(88L);
        stored.setTaskName("detail composition");
        stored.setToolCode("imagemagick");
        stored.setInputJson("{\"taskName\":\"detail composition\",\"moduleOrder\":[\"Hero\"]}");
        stored.setStatus("SUCCEEDED");
        stored.setOutputPath("exports/detail-compositions/final.png");
        stored.setCreateTime(LocalDateTime.now());
        stored.setUpdateTime(LocalDateTime.now());

        RecordingDetailCompositionMapper compositionMapper = new RecordingDetailCompositionMapper();
        compositionMapper.stored.add(stored);
        DetailCompositionServiceImpl service = service(compositionMapper, unavailableToolService(), null);

        DetailCompositionDTO dto = service.getDetailCompositionById(701L);
        PageResult<DetailCompositionDTO> page = service.listDetailCompositions(new DetailCompositionListQuery());

        assertEquals(701L, dto.getId());
        assertEquals(88L, dto.getProductDetailId());
        assertEquals("imagemagick", dto.getToolCode());
        assertEquals("detail composition", dto.getInputData().get("taskName"));
        assertEquals(1, dto.getModuleOrder().size());
        assertEquals(1, page.getData().size());
        assertEquals(701L, page.getData().get(0).getId());
    }

    private static DetailCompositionServiceImpl service(
            RecordingDetailCompositionMapper compositionMapper,
            ToolAdapterService toolAdapterService,
            String imagePath
    ) {
        DetailCompositionServiceImpl service = new DetailCompositionServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", compositionMapper.proxy());
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(service, "toolAdapterService", toolAdapterService);
        ReflectionTestUtils.setField(service, "taskExecutor", null);
        ReflectionTestUtils.setField(service, "productDetailMapper", new ProductDetailMapperStub(imagePath).proxy());
        ReflectionTestUtils.setField(service, "detailCompositionResultMapper", emptyResultMapper());
        return service;
    }

    private static ToolAdapterService unavailableToolService() {
        return (ToolAdapterService) Proxy.newProxyInstance(
                ToolAdapterService.class.getClassLoader(),
                new Class<?>[]{ToolAdapterService.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getTool" -> new ToolAdapterInfoDTO(
                            String.valueOf(args[0]),
                            "ImageMagick",
                            "COMPOSITION_EXPORT",
                            null,
                            null,
                            null,
                            null,
                            null,
                            "compose",
                            "/compose",
                            List.of("compose"),
                            false,
                            "NOT_CONFIGURED");
                    case "listTools" -> List.of();
                    case "getBaseUrl" -> "";
                    default -> null;
                });
    }

    private static DetailCompositionResultMapper emptyResultMapper() {
        return (DetailCompositionResultMapper) Proxy.newProxyInstance(
                DetailCompositionResultMapper.class.getClassLoader(),
                new Class<?>[]{DetailCompositionResultMapper.class},
                (proxy, method, args) -> null);
    }

    private static class ProductDetailMapperStub {
        private final ProductDetail proxyDetail;

        private ProductDetailMapperStub(String imagePath) {
            this.proxyDetail = productDetail(imagePath);
        }

        private ProductDetailMapper proxy() {
            return (ProductDetailMapper) Proxy.newProxyInstance(
                    ProductDetailMapper.class.getClassLoader(),
                    new Class<?>[]{ProductDetailMapper.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "selectById" -> {
                            Serializable id = (Serializable) args[0];
                            yield proxyDetail.getId().equals(id) ? proxyDetail : null;
                        }
                        default -> null;
                    });
        }
    }

    private static ProductDetail productDetail(String imagePath) {
        ProductDetail detail = new ProductDetail();
        detail.setId(88L);
        detail.setTitle("Hero banner");
        detail.setProductName("Hero banner");
        detail.setModuleOrder("[\"Hero\"]");
        if (imagePath != null) {
            detail.setImages("[\"" + imagePath.replace("\\", "\\\\") + "\"]");
        }
        return detail;
    }

    private static class RecordingDetailCompositionMapper {
        private final List<DetailComposition> stored = new ArrayList<>();
        private DetailComposition insertedComposition;
        private DetailComposition updatedComposition;

        private DetailCompositionMapper proxy() {
            return (DetailCompositionMapper) Proxy.newProxyInstance(
                    DetailCompositionMapper.class.getClassLoader(),
                    new Class<?>[]{DetailCompositionMapper.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "selectById" -> {
                            Serializable id = (Serializable) args[0];
                            yield stored.stream()
                                    .filter(item -> item.getId() != null && item.getId().equals(id))
                                    .findFirst()
                                    .orElse(null);
                        }
                        case "selectList" -> new ArrayList<>(stored);
                        case "selectPage" -> {
                            @SuppressWarnings("unchecked")
                            com.baomidou.mybatisplus.extension.plugins.pagination.Page<DetailComposition> page =
                                    (com.baomidou.mybatisplus.extension.plugins.pagination.Page<DetailComposition>) args[0];
                            page.setRecords(new ArrayList<>(stored));
                            page.setTotal(stored.size());
                            yield page;
                        }
                        case "insert" -> {
                            insertedComposition = (DetailComposition) args[0];
                            insertedComposition.setId(401L);
                            stored.add(insertedComposition);
                            yield 1;
                        }
                        case "updateById" -> {
                            updatedComposition = (DetailComposition) args[0];
                            stored.removeIf(item -> item.getId() != null && item.getId().equals(updatedComposition.getId()));
                            stored.add(updatedComposition);
                            yield 1;
                        }
                        default -> null;
                    });
        }
    }

    private File createTempPngFile(String relativePath, int width, int height) {
        try {
            Path path = Path.of(relativePath).toAbsolutePath();
            Files.createDirectories(path.getParent());
            java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            javax.imageio.ImageIO.write(image, "png", path.toFile());
            path.toFile().deleteOnExit();
            return path.toFile();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
