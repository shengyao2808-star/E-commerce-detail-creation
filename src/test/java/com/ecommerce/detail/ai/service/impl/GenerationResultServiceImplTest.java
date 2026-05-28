package com.ecommerce.detail.ai.service.impl;

import com.ecommerce.detail.ai.dto.GenerationResultDTO;
import com.ecommerce.detail.ai.dto.GenerationResultListQuery;
import com.ecommerce.detail.ai.dto.GenerationResultSelectionDTO;
import com.ecommerce.detail.ai.entity.GenerationResult;
import com.ecommerce.detail.ai.mapper.GenerationResultMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerationResultServiceImplTest {

    @Test
    void saveGenerationResultPersistsRealResultJsonAndSelectionFlag() {
        RecordingGenerationResultMapper mapper = new RecordingGenerationResultMapper(null);
        GenerationResultServiceImpl service = service(mapper);

        GenerationResultDTO dto = new GenerationResultDTO();
        dto.setImageJobId(500L);
        dto.setResultUrl("https://cdn.example/result.png");
        dto.setThumbnailUrl("https://cdn.example/thumb.png");
        dto.setPrompt("a clean product banner");
        dto.setParams(Map.of("seed", 1234));
        dto.setComplianceStatus("APPROVED");
        dto.setSelected(true);

        Long id = service.saveGenerationResult(dto);

        assertEquals(501L, id);
        assertEquals(1, mapper.insertCount);
        assertTrue(mapper.insertedResult.getParamsJson().contains("\"seed\""));
        assertEquals(1, mapper.insertedResult.getSelected());
    }

    @Test
    void getGenerationResultByIdReturnsStoredValues() {
        GenerationResult result = new GenerationResult();
        result.setId(502L);
        result.setImageJobId(500L);
        result.setResultUrl("https://cdn.example/result.png");
        result.setThumbnailUrl("https://cdn.example/thumb.png");
        result.setPrompt("stored prompt");
        result.setParamsJson("{\"seed\":1234}");
        result.setComplianceStatus("APPROVED");
        result.setSelected(1);

        RecordingGenerationResultMapper mapper = new RecordingGenerationResultMapper(result);
        GenerationResultServiceImpl service = service(mapper);

        GenerationResultDTO dto = service.getGenerationResultById(502L);

        assertEquals(502L, dto.getId());
        assertEquals("stored prompt", dto.getPrompt());
        assertEquals(1234, ((Number) dto.getParams().get("seed")).intValue());
        assertEquals(true, dto.getSelected());
    }

    @Test
    void updateSelectionTogglesPersistedFlag() {
        GenerationResult result = new GenerationResult();
        result.setId(503L);
        result.setImageJobId(500L);
        result.setSelected(0);

        RecordingGenerationResultMapper mapper = new RecordingGenerationResultMapper(result);
        GenerationResultServiceImpl service = service(mapper);

        GenerationResultSelectionDTO dto = new GenerationResultSelectionDTO();
        dto.setSelected(true);

        boolean updated = service.updateGenerationResultSelection(503L, dto);

        assertEquals(true, updated);
        assertEquals(1, mapper.updatedResult.getSelected());
    }

    @Test
    void upsertGenerationResultUpdatesExistingRowAndPreservesSelection() {
        GenerationResult result = new GenerationResult();
        result.setId(504L);
        result.setImageJobId(500L);
        result.setResultUrl("https://cdn.example/result.png");
        result.setThumbnailUrl("https://cdn.example/old-thumb.png");
        result.setPrompt("old prompt");
        result.setParamsJson("{\"seed\":1}");
        result.setComplianceStatus("APPROVED");
        result.setSelected(1);

        RecordingGenerationResultMapper mapper = new RecordingGenerationResultMapper(result);
        GenerationResultServiceImpl service = service(mapper);

        GenerationResultDTO dto = new GenerationResultDTO();
        dto.setImageJobId(500L);
        dto.setResultUrl("https://cdn.example/result.png");
        dto.setThumbnailUrl("");
        dto.setPrompt("");
        dto.setParams(Map.of());
        dto.setSelected(false);

        Long id = service.upsertGenerationResult(dto);

        assertEquals(504L, id);
        assertEquals(0, mapper.insertCount);
        assertEquals(1, mapper.updateCount);
        assertEquals(1, mapper.updatedResult.getSelected());
        assertEquals("https://cdn.example/old-thumb.png", mapper.updatedResult.getThumbnailUrl());
        assertEquals("old prompt", mapper.updatedResult.getPrompt());
        assertEquals("{\"seed\":1}", mapper.updatedResult.getParamsJson());
        assertEquals("APPROVED", mapper.updatedResult.getComplianceStatus());
    }

    @Test
    void upsertGenerationResultRejectsBlankResultUrl() {
        RecordingGenerationResultMapper mapper = new RecordingGenerationResultMapper(null);
        GenerationResultServiceImpl service = service(mapper);

        GenerationResultDTO dto = new GenerationResultDTO();
        dto.setImageJobId(500L);
        dto.setResultUrl("  ");

        assertThrows(IllegalArgumentException.class, () -> service.upsertGenerationResult(dto));
    }

    @Test
    void listGenerationResultsDefaultsToEmptyWhenNoDataExists() {
        RecordingGenerationResultMapper mapper = new RecordingGenerationResultMapper(null);
        GenerationResultServiceImpl service = service(mapper);

        var page = service.listGenerationResults(new GenerationResultListQuery());

        assertEquals(0L, page.getTotal());
        assertTrue(page.getData().isEmpty());
    }

    private static GenerationResultServiceImpl service(RecordingGenerationResultMapper mapper) {
        GenerationResultServiceImpl service = new GenerationResultServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper.proxy());
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        return service;
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

    private static class RecordingGenerationResultMapper {
        private final GenerationResult storedResult;
        private GenerationResult insertedResult;
        private GenerationResult updatedResult;
        private int insertCount;
        private int updateCount;

        private RecordingGenerationResultMapper(GenerationResult storedResult) {
            this.storedResult = storedResult;
        }

        private GenerationResultMapper proxy() {
            return (GenerationResultMapper) Proxy.newProxyInstance(
                    GenerationResultMapper.class.getClassLoader(),
                    new Class<?>[]{GenerationResultMapper.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();
                        if ("selectById".equals(methodName)) {
                            Serializable id = (Serializable) args[0];
                            return storedResult != null && storedResult.getId().equals(id) ? storedResult : null;
                        }
                        if ("selectOne".equals(methodName)) {
                            return storedResult;
                        }
                        if ("selectPage".equals(methodName)) {
                            @SuppressWarnings("unchecked")
                            Page<GenerationResult> page = (Page<GenerationResult>) args[0];
                            page.setRecords(List.of());
                            page.setTotal(0);
                            return page;
                        }
                        if ("insert".equals(methodName)) {
                            insertedResult = (GenerationResult) args[0];
                            insertedResult.setId(501L);
                            insertCount++;
                            return 1;
                        }
                        if ("updateById".equals(methodName)) {
                            updatedResult = (GenerationResult) args[0];
                            updateCount++;
                            return 1;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }
}
