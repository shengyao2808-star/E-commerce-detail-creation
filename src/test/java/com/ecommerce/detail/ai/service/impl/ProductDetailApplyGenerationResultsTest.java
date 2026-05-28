package com.ecommerce.detail.ai.service.impl;

import com.ecommerce.detail.ai.dto.ApplyGenerationResultsDTO;
import com.ecommerce.detail.ai.entity.DetailGenerationResultLink;
import com.ecommerce.detail.ai.entity.GenerationResult;
import com.ecommerce.detail.ai.entity.ProductDetail;
import com.ecommerce.detail.ai.mapper.DetailGenerationResultLinkMapper;
import com.ecommerce.detail.ai.mapper.GenerationResultMapper;
import com.ecommerce.detail.ai.mapper.ProductDetailMapper;
import com.ecommerce.detail.ai.util.RiskCheckUtil;
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

class ProductDetailApplyGenerationResultsTest {

    @Test
    void applyGenerationResultsMergesSelectedResultUrlsIntoExistingImagesAndCreatesTraceLinks() {
        ProductState state = new ProductState();
        ProductDetail detail = new ProductDetail();
        detail.setId(100L);
        detail.setTitle("Detail title");
        detail.setImages("[\"https://cdn.example.com/existing.png\",\"https://cdn.example.com/shared.png\"]");
        state.detail = detail;

        GenerationResult shared = generationResult(11L, "https://cdn.example.com/shared.png", true);
        GenerationResult fresh = generationResult(12L, "https://cdn.example.com/fresh.png", true);
        state.generationResults.put(shared.getId(), shared);
        state.generationResults.put(fresh.getId(), fresh);

        ProductDetailServiceImpl service = service(state);

        int applied = service.applyGenerationResults(100L, applyRequest(List.of(11L, 12L), true));

        assertEquals(2, applied);
        assertEquals("[\"https://cdn.example.com/existing.png\",\"https://cdn.example.com/shared.png\",\"https://cdn.example.com/fresh.png\"]",
                state.updatedDetail.getImages());
        assertEquals(2, state.links.size());
        assertTrue(state.links.values().stream().anyMatch(link -> link.getGenerationResultId().equals(11L)));
        assertTrue(state.links.values().stream().anyMatch(link -> link.getGenerationResultId().equals(12L)));
    }

    @Test
    void applyGenerationResultsRejectsEmptySelection() {
        ProductState state = new ProductState();
        ProductDetail detail = new ProductDetail();
        detail.setId(101L);
        detail.setTitle("Detail title");
        detail.setImages("[\"https://cdn.example.com/existing.png\"]");
        state.detail = detail;

        ProductDetailServiceImpl service = service(state);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.applyGenerationResults(101L, applyRequest(List.of(), true)));
        assertTrue(ex.getMessage().toLowerCase().contains("selected"));
    }

    @Test
    void applyGenerationResultsRejectsBlankResultUrl() {
        ProductState state = new ProductState();
        ProductDetail detail = new ProductDetail();
        detail.setId(102L);
        detail.setTitle("Detail title");
        detail.setImages("[\"https://cdn.example.com/existing.png\"]");
        state.detail = detail;

        GenerationResult broken = generationResult(21L, "   ", true);
        state.generationResults.put(broken.getId(), broken);

        ProductDetailServiceImpl service = service(state);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.applyGenerationResults(102L, applyRequest(List.of(21L), false)));
        assertTrue(ex.getMessage().toLowerCase().contains("result"));
    }

    private ProductDetailServiceImpl service(ProductState state) {
        ProductDetailServiceImpl service = new ProductDetailServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", proxy(ProductDetailMapper.class, invocation -> {
            if ("selectById".equals(invocation.getMethod().getName())) {
                Serializable id = (Serializable) invocation.getArguments()[0];
                return state.detail != null && state.detail.getId().equals(id) ? state.detail : null;
            }
            if ("updateById".equals(invocation.getMethod().getName())) {
                state.updatedDetail = (ProductDetail) invocation.getArguments()[0];
                return 1;
            }
            return defaultValue(invocation.getMethod().getReturnType());
        }));
        ReflectionTestUtils.setField(service, "generationResultMapper", proxy(GenerationResultMapper.class, invocation -> {
            String method = invocation.getMethod().getName();
            if ("selectById".equals(method)) {
                Serializable id = (Serializable) invocation.getArguments()[0];
                return state.generationResults.get(id);
            }
            if ("selectBatchIds".equals(method)) {
                @SuppressWarnings("unchecked")
                Iterable<Long> ids = (Iterable<Long>) invocation.getArguments()[0];
                List<GenerationResult> results = new ArrayList<>();
                for (Long id : ids) {
                    GenerationResult result = state.generationResults.get(id);
                    if (result != null) {
                        results.add(result);
                    }
                }
                return results;
            }
            if ("selectList".equals(method)) {
                return new ArrayList<>(state.generationResults.values());
            }
            return defaultValue(invocation.getMethod().getReturnType());
        }));
        ReflectionTestUtils.setField(service, "detailGenerationResultLinkMapper", proxy(DetailGenerationResultLinkMapper.class, invocation -> {
            String method = invocation.getMethod().getName();
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
            if ("selectList".equals(method)) {
                return new ArrayList<>(state.links.values());
            }
            return defaultValue(invocation.getMethod().getReturnType());
        }));
        ReflectionTestUtils.setField(service, "riskCheckUtil", new RiskCheckUtil());
        return service;
    }

    private ApplyGenerationResultsDTO applyRequest(List<Long> generationResultIds, boolean selectedOnly) {
        ApplyGenerationResultsDTO dto = new ApplyGenerationResultsDTO();
        dto.setGenerationResultIds(generationResultIds);
        dto.setSelectedOnly(selectedOnly);
        return dto;
    }

    private GenerationResult generationResult(Long id, String resultUrl, boolean selected) {
        GenerationResult result = new GenerationResult();
        result.setId(id);
        result.setImageJobId(500L + id);
        result.setResultUrl(resultUrl);
        result.setThumbnailUrl(resultUrl);
        result.setPrompt("prompt-" + id);
        result.setParamsJson("{\"ratio\":\"1:1\"}");
        result.setComplianceStatus("APPROVED");
        result.setSelected(selected ? 1 : 0);
        return result;
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

    private static class ProductState {
        private ProductDetail detail;
        private ProductDetail updatedDetail;
        private final Map<Long, GenerationResult> generationResults = new LinkedHashMap<>();
        private final Map<Long, DetailGenerationResultLink> links = new LinkedHashMap<>();
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
}
