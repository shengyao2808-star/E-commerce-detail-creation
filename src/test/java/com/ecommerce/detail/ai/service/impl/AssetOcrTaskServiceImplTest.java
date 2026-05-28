package com.ecommerce.detail.ai.service.impl;

import com.ecommerce.detail.ai.dto.AssetOcrTaskDTO;
import com.ecommerce.detail.ai.dto.AssetOcrTaskResultDTO;
import com.ecommerce.detail.ai.entity.AssetOcrTask;
import com.ecommerce.detail.ai.mapper.AssetOcrTaskMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssetOcrTaskServiceImplTest {

    @Test
    void getAssetOcrTaskReturnsEmptyTextAndZeroConfidenceWhenNoResultExists() {
        AssetOcrTask task = new AssetOcrTask();
        task.setId(21L);
        task.setAssetName("sample.png");
        task.setLanguage("eng");
        task.setStatus("PENDING");
        task.setOcrText(null);
        task.setConfidence(null);

        RecordingAssetOcrTaskMapper mapper = new RecordingAssetOcrTaskMapper(task);
        AssetOcrTaskServiceImpl service = service(mapper);

        AssetOcrTaskDTO dto = service.getAssetOcrTaskById(21L);

        assertEquals(21L, dto.getId());
        assertEquals("", dto.getOcrText());
        assertEquals(0D, dto.getConfidence());
    }

    @Test
    void updateAssetOcrTaskResultPersistsRealTextAndConfidence() {
        AssetOcrTask task = new AssetOcrTask();
        task.setId(22L);
        task.setAssetName("ocr-target.jpg");
        task.setStatus("RUNNING");

        RecordingAssetOcrTaskMapper mapper = new RecordingAssetOcrTaskMapper(task);
        AssetOcrTaskServiceImpl service = service(mapper);

        AssetOcrTaskResultDTO dto = new AssetOcrTaskResultDTO();
        dto.setOcrText("REAL OCR OUTPUT");
        dto.setConfidence(92.5D);
        dto.setProgress(100);

        boolean updated = service.updateAssetOcrTaskResult(22L, dto);

        assertTrue(updated);
        assertEquals(1, mapper.updateCount);
        assertEquals("SUCCEEDED", mapper.updatedTask.getStatus());
        assertEquals("REAL OCR OUTPUT", mapper.updatedTask.getOcrText());
        assertEquals(92.5D, mapper.updatedTask.getConfidence());
    }

    @Test
    void updateAssetOcrTaskStatusRejectsUnsupportedStatus() {
        AssetOcrTask task = new AssetOcrTask();
        task.setId(23L);
        task.setStatus("PENDING");

        RecordingAssetOcrTaskMapper mapper = new RecordingAssetOcrTaskMapper(task);
        AssetOcrTaskServiceImpl service = service(mapper);

        com.ecommerce.detail.ai.dto.AssetOcrTaskStatusDTO dto = new com.ecommerce.detail.ai.dto.AssetOcrTaskStatusDTO();
        dto.setStatus("done");

        assertThrows(IllegalArgumentException.class, () -> service.updateAssetOcrTaskStatus(23L, dto));
    }

    private static AssetOcrTaskServiceImpl service(RecordingAssetOcrTaskMapper mapper) {
        AssetOcrTaskServiceImpl service = new AssetOcrTaskServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper.proxy());
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

    private static class RecordingAssetOcrTaskMapper {
        private final AssetOcrTask storedTask;
        private AssetOcrTask updatedTask;
        private int updateCount;

        private RecordingAssetOcrTaskMapper(AssetOcrTask storedTask) {
            this.storedTask = storedTask;
        }

        private AssetOcrTaskMapper proxy() {
            return (AssetOcrTaskMapper) Proxy.newProxyInstance(
                    AssetOcrTaskMapper.class.getClassLoader(),
                    new Class<?>[]{AssetOcrTaskMapper.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();
                        if ("selectById".equals(methodName)) {
                            Serializable id = (Serializable) args[0];
                            return storedTask != null && storedTask.getId().equals(id) ? storedTask : null;
                        }
                        if ("updateById".equals(methodName)) {
                            updatedTask = (AssetOcrTask) args[0];
                            updateCount++;
                            return 1;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }
}
