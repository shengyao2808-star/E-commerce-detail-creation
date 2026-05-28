package com.ecommerce.detail.ai.service.impl;

import com.ecommerce.detail.ai.dto.DesignDraftDTO;
import com.ecommerce.detail.ai.entity.DesignDraft;
import com.ecommerce.detail.ai.mapper.DesignDraftMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DesignDraftServiceImplTest {

    @Test
    void createDesignDraftPersistsSceneAndSelectedAssetsJson() {
        RecordingDesignDraftMapper mapper = new RecordingDesignDraftMapper(null);
        DesignDraftServiceImpl service = service(mapper);

        DesignDraftDTO dto = new DesignDraftDTO();
        dto.setDraftName("首页草稿");
        dto.setSceneJson("{\"type\":\"excalidraw\"}");
        dto.setSelectedAssets(List.of(Map.of("id", "a1", "name", "hero.png")));
        dto.setStatus("PENDING");

        Long id = service.createDesignDraft(dto);

        assertEquals(201L, id);
        assertEquals(1, mapper.insertCount);
        assertEquals("{\"type\":\"excalidraw\"}", mapper.insertedDraft.getSceneJson());
        assertTrue(mapper.insertedDraft.getSelectedAssetsJson().contains("\"hero.png\""));
    }

    @Test
    void getDesignDraftReturnsStoredSceneAndSelectedAssets() {
        DesignDraft draft = new DesignDraft();
        draft.setId(202L);
        draft.setDraftName("已存草稿");
        draft.setSceneJson("{\"elements\":[]}");
        draft.setSelectedAssetsJson("[{\"id\":\"a2\",\"name\":\"detail.png\"}]");
        draft.setStatus("SUCCEEDED");

        RecordingDesignDraftMapper mapper = new RecordingDesignDraftMapper(draft);
        DesignDraftServiceImpl service = service(mapper);

        DesignDraftDTO dto = service.getDesignDraftById(202L);

        assertEquals(202L, dto.getId());
        assertEquals("{\"elements\":[]}", dto.getSceneJson());
        assertEquals(1, dto.getSelectedAssets().size());
        assertEquals("detail.png", dto.getSelectedAssets().get(0).get("name"));
    }

    @Test
    void createDesignDraftRejectsUnsupportedStatus() {
        RecordingDesignDraftMapper mapper = new RecordingDesignDraftMapper(null);
        DesignDraftServiceImpl service = service(mapper);

        DesignDraftDTO dto = new DesignDraftDTO();
        dto.setDraftName("invalid-status");
        dto.setStatus("completed");

        assertThrows(IllegalArgumentException.class, () -> service.createDesignDraft(dto));
    }

    private static DesignDraftServiceImpl service(RecordingDesignDraftMapper mapper) {
        DesignDraftServiceImpl service = new DesignDraftServiceImpl();
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

    private static class RecordingDesignDraftMapper {
        private final DesignDraft storedDraft;
        private DesignDraft insertedDraft;
        private int insertCount;

        private RecordingDesignDraftMapper(DesignDraft storedDraft) {
            this.storedDraft = storedDraft;
        }

        private DesignDraftMapper proxy() {
            return (DesignDraftMapper) Proxy.newProxyInstance(
                    DesignDraftMapper.class.getClassLoader(),
                    new Class<?>[]{DesignDraftMapper.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();
                        if ("selectById".equals(methodName)) {
                            Serializable id = (Serializable) args[0];
                            return storedDraft != null && storedDraft.getId().equals(id) ? storedDraft : null;
                        }
                        if ("insert".equals(methodName)) {
                            insertedDraft = (DesignDraft) args[0];
                            insertedDraft.setId(201L);
                            insertCount++;
                            return 1;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }
}
