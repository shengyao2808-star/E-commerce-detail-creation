package com.ecommerce.detail.ai.service.impl;

import com.ecommerce.detail.ai.dto.PromptWorkbenchEntryDTO;
import com.ecommerce.detail.ai.dto.PromptWorkbenchRequestDTO;
import com.ecommerce.detail.ai.dto.tool.ToolAdapterInfoDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeResponseDTO;
import com.ecommerce.detail.ai.entity.PromptWorkbenchEntry;
import com.ecommerce.detail.ai.mapper.PromptWorkbenchEntryMapper;
import com.ecommerce.detail.ai.service.ToolAdapterService;
import com.ecommerce.detail.ai.util.AIUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PromptWorkbenchServiceImplTest {

    @Test
    void guidedPromptPersistsRelayOutputAndMarksCanceledWhenRelayUnavailable() {
        RecordingPromptWorkbenchMapper mapper = new RecordingPromptWorkbenchMapper(null);
        PromptWorkbenchServiceImpl service = service(mapper, relayAiUtil("guided output"), toolService(false, null));

        PromptWorkbenchRequestDTO dto = new PromptWorkbenchRequestDTO();
        dto.setTaskName("引导生成");
        dto.setPromptText("生成女装主图 prompt");
        dto.setInputData(Map.of("brand", "demo"));

        PromptWorkbenchEntryDTO result = service.createGuidedPrompt(dto);
        assertEquals("SUCCEEDED", result.getStatus());
        assertEquals(1, result.getVersion());
        assertEquals("guided output", result.getOutputText());
        assertEquals("guided output", result.getOutputData().get("text"));
        assertEquals("生成女装主图 prompt", ((Map<?, ?>) result.getInputData()).get("promptText"));
        assertEquals("GUIDED", result.getInputData().get("entryType"));
        assertEquals("ai-relay", result.getInputData().get("toolCode"));

        PromptWorkbenchServiceImpl canceledService = service(new RecordingPromptWorkbenchMapper(null),
                relayUnavailableAiUtil(), toolService(false, null));
        PromptWorkbenchEntryDTO canceled = canceledService.createGuidedPrompt(dto);
        assertEquals("CANCELED", canceled.getStatus());
        assertEquals(1, canceled.getVersion());
    }

    @Test
    void guidedPromptAcceptsStructuredInputWithoutPromptText() {
        RecordingPromptWorkbenchMapper mapper = new RecordingPromptWorkbenchMapper(null);
        PromptWorkbenchServiceImpl service = service(
                mapper,
                relayAiUtil("{\"positivePrompt\":\"clean studio shot\",\"riskWarnings\":[\"check logo\"]}"),
                toolService(false, null)
        );

        PromptWorkbenchRequestDTO dto = new PromptWorkbenchRequestDTO();
        dto.setTaskName("guided from structured input");
        dto.setCategoryCode("WOMEN_DRESS");
        dto.setInputData(Map.of(
                "brandGuideline", "clean and premium",
                "platformRequirement", "hero image",
                "ratio", "1:1",
                "referenceNotes", "focus on fabric texture"
        ));

        PromptWorkbenchEntryDTO result = service.createGuidedPrompt(dto);
        assertEquals("SUCCEEDED", result.getStatus());
        assertEquals("clean studio shot", ((Map<?, ?>) result.getOutputData().get("body")).get("positivePrompt"));
        assertEquals(List.of("check logo"), result.getRiskWarnings());
        assertEquals("AI_RELAY", ((Map<?, ?>) result.getSourceData()).get("sourceType"));
    }

    @Test
    void expandPromptAcceptsPositivePromptFromStructuredInput() {
        RecordingPromptWorkbenchMapper mapper = new RecordingPromptWorkbenchMapper(null);
        PromptWorkbenchServiceImpl service = service(
                mapper,
                relayAiUtil("{\"positivePrompt\":\"expanded prompt\",\"negativePrompt\":\"low quality\"}"),
                toolService(false, null)
        );

        PromptWorkbenchRequestDTO dto = new PromptWorkbenchRequestDTO();
        dto.setTaskName("expand from structured input");
        dto.setInputData(Map.of(
                "positivePrompt", "base prompt",
                "negativePrompt", "bad anatomy",
                "styleTags", List.of("clean", "catalog")
        ));

        PromptWorkbenchEntryDTO result = service.expandPrompt(dto);
        assertEquals("SUCCEEDED", result.getStatus());
        assertEquals("expanded prompt", ((Map<?, ?>) result.getOutputData().get("body")).get("positivePrompt"));
        assertEquals("AI_RELAY", ((Map<?, ?>) result.getSourceData()).get("sourceType"));
    }

    @Test
    void imageToPromptPersistsToolOutputAndFailsClosedOnToolError() {
        RecordingPromptWorkbenchMapper mapper = new RecordingPromptWorkbenchMapper(null);
        PromptWorkbenchServiceImpl service = service(mapper, relayUnavailableAiUtil(), toolService(true,
                new ToolInvokeResponseDTO("llava", "image-to-prompt", 200,
                        Map.of("caption", "product photo"), "{\"caption\":\"product photo\"}")));

        PromptWorkbenchRequestDTO dto = new PromptWorkbenchRequestDTO();
        dto.setTaskName("图转 prompt");
        dto.setImageUrl("file:///tmp/reference.png");
        dto.setInputData(Map.of("imageUrl", "file:///tmp/reference.png", "language", "zh-CN"));

        PromptWorkbenchEntryDTO result = service.imageToPrompt(dto);
        assertEquals("SUCCEEDED", result.getStatus());
        assertEquals(1, result.getVersion());
        assertEquals("product photo", result.getOutputText());
        assertEquals("IMAGE_TO_PROMPT", result.getInputData().get("entryType"));
        assertEquals("llava", result.getInputData().get("toolCode"));
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) result.getOutputData().get("body");
        assertEquals("product photo", body.get("caption"));
        assertEquals("TOOL_ADAPTER", ((Map<?, ?>) result.getSourceData()).get("sourceType"));

        PromptWorkbenchServiceImpl canceledService = service(new RecordingPromptWorkbenchMapper(null),
                relayUnavailableAiUtil(), toolService(false, null));
        PromptWorkbenchEntryDTO canceled = canceledService.imageToPrompt(dto);
        assertEquals("CANCELED", canceled.getStatus());
        assertEquals(1, canceled.getVersion());

        PromptWorkbenchServiceImpl failedService = service(new RecordingPromptWorkbenchMapper(null),
                relayUnavailableAiUtil(), toolService(true, null));
        PromptWorkbenchEntryDTO failed = failedService.imageToPrompt(dto);
        assertEquals("FAILED", failed.getStatus());
        assertEquals(1, failed.getVersion());
    }

    @Test
    void guidedPromptRejectsBlankPromptTextWhenRelayRequiresContent() {
        PromptWorkbenchServiceImpl service = service(new RecordingPromptWorkbenchMapper(null), relayAiUtil("guided output"), toolService(false, null));
        PromptWorkbenchRequestDTO dto = new PromptWorkbenchRequestDTO();
        dto.setTaskName("引导生成");
        assertThrows(IllegalArgumentException.class, () -> service.createGuidedPrompt(dto));
    }

    private PromptWorkbenchServiceImpl service(RecordingPromptWorkbenchMapper mapper, AIUtil aiUtil, ToolAdapterService toolAdapterService) {
        PromptWorkbenchServiceImpl service = new PromptWorkbenchServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper.proxy());
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(service, "aiUtil", aiUtil);
        ReflectionTestUtils.setField(service, "toolAdapterService", toolAdapterService);
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

    private ToolAdapterService toolService(boolean configured, ToolInvokeResponseDTO response) {
        return (ToolAdapterService) Proxy.newProxyInstance(
                ToolAdapterService.class.getClassLoader(),
                new Class<?>[]{ToolAdapterService.class},
                (proxy, method, args) -> {
                    return switch (method.getName()) {
                        case "getTool" -> new ToolAdapterInfoDTO("llava", "LLaVA", "IMAGE_TO_PROMPT", null, null, null, null, null, "image-to-prompt", "/v1/chat/completions", List.of("image-to-prompt"), configured, configured ? "CONFIGURED" : "NOT_CONFIGURED");
                        case "invoke" -> {
                            if (!configured) {
                                throw new UnsupportedOperationException("Tool adapter not configured: llava");
                            }
                            if (response == null) {
                                throw new RuntimeException("tool failure");
                            }
                            yield response;
                        }
                        case "listTools" -> List.of();
                        case "getBaseUrl" -> "";
                        default -> defaultValue(method.getReturnType());
                    };
                });
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

    private static class RecordingPromptWorkbenchMapper {
        private final PromptWorkbenchEntry stored;
        private PromptWorkbenchEntry inserted;

        private RecordingPromptWorkbenchMapper(PromptWorkbenchEntry stored) {
            this.stored = stored;
        }

        private PromptWorkbenchEntryMapper proxy() {
            return (PromptWorkbenchEntryMapper) Proxy.newProxyInstance(
                    PromptWorkbenchEntryMapper.class.getClassLoader(),
                    new Class<?>[]{PromptWorkbenchEntryMapper.class},
                    (proxy, method, args) -> {
                        if ("selectById".equals(method.getName())) {
                            Serializable id = (Serializable) args[0];
                            return stored != null && stored.getId().equals(id) ? stored : null;
                        }
                        if ("insert".equals(method.getName())) {
                            inserted = (PromptWorkbenchEntry) args[0];
                            inserted.setId(601L);
                            return 1;
                        }
                        if ("updateById".equals(method.getName())) {
                            inserted = (PromptWorkbenchEntry) args[0];
                            return 1;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }
}
