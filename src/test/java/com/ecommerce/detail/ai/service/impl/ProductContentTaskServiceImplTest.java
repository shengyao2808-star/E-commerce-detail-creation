package com.ecommerce.detail.ai.service.impl;

import com.ecommerce.detail.ai.dto.ProductContentTaskApplyDTO;
import com.ecommerce.detail.ai.dto.ProductContentTaskDTO;
import com.ecommerce.detail.ai.dto.ProductContentTaskRequestDTO;
import com.ecommerce.detail.ai.entity.ProductContentTask;
import com.ecommerce.detail.ai.entity.ProductDetail;
import com.ecommerce.detail.ai.mapper.ProductContentTaskMapper;
import com.ecommerce.detail.ai.mapper.ProductDetailMapper;
import com.ecommerce.detail.ai.util.AIUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductContentTaskServiceImplTest {

    private static final String STRUCTURED_OUTPUT = """
            {
              "title": "Generated title",
              "subtitle": "Generated subtitle",
              "sellingPoints": ["soft fabric", "machine washable"],
              "detailModules": [{"moduleKey":"fabric","copy":"Soft cotton blend"}],
              "faq": [{"question":"Can it be washed?","answer":"Use cold water."}],
              "seoKeywords": ["summer dress", "cotton dress"],
              "riskWarnings": ["verify fabric claim"]
            }
            """;

    @Test
    void createTaskPersistsInputRelayOutputAndStructuredFields() {
        RecordingContentTaskMapper taskMapper = new RecordingContentTaskMapper(null);
        ProductContentTaskServiceImpl service = service(taskMapper, new RecordingProductDetailMapper(null), relayAiUtil(STRUCTURED_OUTPUT));

        ProductContentTaskRequestDTO request = new ProductContentTaskRequestDTO();
        request.setTaskName("content draft");
        request.setProductDetailId(101L);
        request.setInputData(Map.of("category", "dress", "facts", List.of("cotton")));

        ProductContentTaskDTO result = service.createProductContentTask(request);

        assertEquals("SUCCEEDED", result.getStatus());
        assertEquals(1, result.getVersion());
        assertEquals("Generated title", result.getTitle());
        assertEquals("Generated subtitle", result.getSubtitle());
        assertEquals(List.of("soft fabric", "machine washable"), result.getSellingPoints());
        assertEquals(List.of("summer dress", "cotton dress"), result.getSeoKeywords());
        assertEquals(List.of("verify fabric claim"), result.getRiskWarnings());
        assertEquals("AI_RELAY", result.getSourceData().get("sourceType"));
        assertEquals(101L, ((Number) result.getInputData().get("productDetailId")).longValue());
        assertTrue(taskMapper.current.getOutputJson().contains("\"rawBody\""));
    }

    @Test
    void createTaskMarksCanceledWhenAiRelayIsUnavailableAndFailedWhenExecutionFails() {
        ProductContentTaskRequestDTO request = new ProductContentTaskRequestDTO();
        request.setTaskName("content draft");
        request.setProductDetailId(101L);
        request.setInputData(Map.of("category", "dress"));

        ProductContentTaskServiceImpl canceledService = service(
                new RecordingContentTaskMapper(null),
                new RecordingProductDetailMapper(null),
                relayUnavailableAiUtil()
        );
        ProductContentTaskDTO canceled = canceledService.createProductContentTask(request);
        assertEquals("CANCELED", canceled.getStatus());
        assertNull(canceled.getOutputText());

        ProductContentTaskServiceImpl failedService = service(
                new RecordingContentTaskMapper(null),
                new RecordingProductDetailMapper(null),
                relayFailingAiUtil()
        );
        ProductContentTaskDTO failed = failedService.createProductContentTask(request);
        assertEquals("FAILED", failed.getStatus());
        assertTrue(failed.getErrorMessage().contains("relay exploded"));
    }

    @Test
    void applyTaskUpdatesOnlySelectedFieldsAndKeepsManualContent() {
        ProductContentTask task = new ProductContentTask();
        task.setId(501L);
        task.setProductDetailId(101L);
        task.setTaskName("content draft");
        task.setStatus("SUCCEEDED");
        task.setVersion(2);
        task.setInputJson("{\"productDetailId\":101}");
        task.setOutputText(STRUCTURED_OUTPUT);
        task.setOutputJson("""
                {"body":%s,"rawBody":%s,"source":{"sourceType":"AI_RELAY"}}
                """.formatted(STRUCTURED_OUTPUT, quote(STRUCTURED_OUTPUT)));

        ProductDetail detail = new ProductDetail();
        detail.setId(101L);
        detail.setTitle("Manual title");
        detail.setSubtitle("Manual subtitle");
        detail.setSellingPoints("[\"manual point\"]");
        detail.setSeoKeywords("[\"manual seo\"]");
        detail.setAiGeneratedContent("{\"manual\":true}");

        RecordingContentTaskMapper taskMapper = new RecordingContentTaskMapper(task);
        RecordingProductDetailMapper detailMapper = new RecordingProductDetailMapper(detail);
        ProductContentTaskServiceImpl service = service(taskMapper, detailMapper, relayAiUtil(STRUCTURED_OUTPUT));

        ProductContentTaskApplyDTO apply = new ProductContentTaskApplyDTO();
        apply.setFields(List.of("subtitle", "sellingPoints", "seoKeywords", "aiGeneratedContent"));

        ProductContentTaskDTO applied = service.applyProductContentTask(501L, apply);

        assertEquals("SUCCEEDED", applied.getStatus());
        assertEquals("Manual title", detailMapper.current.getTitle());
        assertEquals("Generated subtitle", detailMapper.current.getSubtitle());
        assertTrue(detailMapper.current.getSellingPoints().contains("soft fabric"));
        assertTrue(detailMapper.current.getSeoKeywords().contains("summer dress"));
        assertTrue(detailMapper.current.getAiGeneratedContent().contains("detailModules"));
        assertEquals("[\"subtitle\",\"sellingPoints\",\"seoKeywords\",\"aiGeneratedContent\"]", taskMapper.current.getAppliedFieldsJson());
    }

    @Test
    void applyTaskRejectsUnsuccessfulTask() {
        ProductContentTask task = new ProductContentTask();
        task.setId(501L);
        task.setProductDetailId(101L);
        task.setStatus("FAILED");

        ProductContentTaskServiceImpl service = service(
                new RecordingContentTaskMapper(task),
                new RecordingProductDetailMapper(new ProductDetail()),
                relayAiUtil(STRUCTURED_OUTPUT)
        );

        ProductContentTaskApplyDTO apply = new ProductContentTaskApplyDTO();
        apply.setFields(List.of("title"));

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> service.applyProductContentTask(501L, apply));
    }

    private ProductContentTaskServiceImpl service(RecordingContentTaskMapper taskMapper,
                                                  RecordingProductDetailMapper detailMapper,
                                                  AIUtil aiUtil) {
        ProductContentTaskServiceImpl service = new ProductContentTaskServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", taskMapper.proxy());
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(service, "productDetailMapper", detailMapper.proxy());
        ReflectionTestUtils.setField(service, "aiUtil", aiUtil);
        return service;
    }

    private AIUtil relayAiUtil(String response) {
        return new AIUtil(new ObjectMapper(), false, "", "", "", 0.7, 128, 1) {
            @Override
            public String relayText(String systemPrompt, String userPrompt) {
                if (userPrompt == null || userPrompt.trim().isEmpty()) {
                    throw new IllegalArgumentException("userPrompt must not be blank");
                }
                return response;
            }
        };
    }

    private AIUtil relayUnavailableAiUtil() {
        return new AIUtil(new ObjectMapper(), false, "", "", "", 0.7, 128, 1) {
            @Override
            public String relayText(String systemPrompt, String userPrompt) {
                throw new UnsupportedOperationException("AI relay is not configured");
            }
        };
    }

    private AIUtil relayFailingAiUtil() {
        return new AIUtil(new ObjectMapper(), false, "", "", "", 0.7, 128, 1) {
            @Override
            public String relayText(String systemPrompt, String userPrompt) {
                throw new RuntimeException("relay exploded after execution");
            }
        };
    }

    private static String quote(String value) {
        try {
            return new ObjectMapper().writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
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

    private static class RecordingContentTaskMapper {
        private ProductContentTask current;

        private RecordingContentTaskMapper(ProductContentTask current) {
            this.current = current;
        }

        private ProductContentTaskMapper proxy() {
            return (ProductContentTaskMapper) Proxy.newProxyInstance(
                    ProductContentTaskMapper.class.getClassLoader(),
                    new Class<?>[]{ProductContentTaskMapper.class},
                    (proxy, method, args) -> {
                        if ("insert".equals(method.getName())) {
                            current = (ProductContentTask) args[0];
                            current.setId(501L);
                            return 1;
                        }
                        if ("selectById".equals(method.getName())) {
                            Serializable id = (Serializable) args[0];
                            return current != null && current.getId().equals(id) ? current : null;
                        }
                        if ("updateById".equals(method.getName())) {
                            current = (ProductContentTask) args[0];
                            return 1;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }

    private static class RecordingProductDetailMapper {
        private ProductDetail current;

        private RecordingProductDetailMapper(ProductDetail current) {
            this.current = current;
        }

        private ProductDetailMapper proxy() {
            return (ProductDetailMapper) Proxy.newProxyInstance(
                    ProductDetailMapper.class.getClassLoader(),
                    new Class<?>[]{ProductDetailMapper.class},
                    (proxy, method, args) -> {
                        if ("selectById".equals(method.getName())) {
                            Serializable id = (Serializable) args[0];
                            return current != null && current.getId().equals(id) ? current : null;
                        }
                        if ("updateById".equals(method.getName())) {
                            current = (ProductDetail) args[0];
                            return 1;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }
}
