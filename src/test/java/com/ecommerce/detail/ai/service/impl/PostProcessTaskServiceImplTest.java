package com.ecommerce.detail.ai.service.impl;

import com.ecommerce.detail.ai.dto.PostProcessTaskCreateDTO;
import com.ecommerce.detail.ai.dto.PostProcessTaskDTO;
import com.ecommerce.detail.ai.dto.tool.ToolAdapterInfoDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeResponseDTO;
import com.ecommerce.detail.ai.entity.PostProcessTask;
import com.ecommerce.detail.ai.mapper.PostProcessTaskMapper;
import com.ecommerce.detail.ai.service.ToolAdapterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostProcessTaskServiceImplTest {

    @Test
    void createTaskCancelsWhenToolIsNotConfigured() {
        RecordingMapper mapper = new RecordingMapper(null);
        PostProcessTaskServiceImpl service = service(mapper, toolService(false, null));

        PostProcessTaskCreateDTO dto = new PostProcessTaskCreateDTO();
        dto.setSourceImagePath("exports/test.png");
        dto.setToolCode("real-esrgan");
        dto.setOperation("upscale");

        Long id = service.createPostProcessTask(dto);

        assertEquals(501L, id);
        assertEquals(1, mapper.insertCount);
        assertEquals("CANCELED", mapper.insertedTask.getStatus());
        assertTrue(mapper.insertedTask.getErrorMessage().contains("not configured"));
    }

    @Test
    void createTaskRunsWhenToolIsConfigured() {
        RecordingMapper mapper = new RecordingMapper(null);
        ToolInvokeResponseDTO response = new ToolInvokeResponseDTO(
                "real-esrgan", "upscale", 200, Map.of("status", "accepted"), "{\"status\":\"accepted\"}");
        PostProcessTaskServiceImpl service = service(mapper, toolService(true, response));

        PostProcessTaskCreateDTO dto = new PostProcessTaskCreateDTO();
        dto.setSourceImagePath("exports/product.png");
        dto.setToolCode("real-esrgan");
        dto.setOperation("upscale");
        dto.setParams(Map.of("scale", 2));

        Long id = service.createPostProcessTask(dto);

        assertEquals(501L, id);
        assertEquals(1, mapper.insertCount);
        assertEquals("RUNNING", mapper.insertedTask.getStatus());
        assertEquals("real-esrgan", mapper.insertedTask.getToolCode());
        assertEquals("upscale", mapper.insertedTask.getOperation());
        assertNotNull(mapper.insertedTask.getParamsJson());
    }

    @Test
    void createTaskRejectsBlankToolCode() {
        RecordingMapper mapper = new RecordingMapper(null);
        PostProcessTaskServiceImpl service = service(mapper, toolService(false, null));

        PostProcessTaskCreateDTO dto = new PostProcessTaskCreateDTO();
        dto.setSourceImagePath("exports/test.png");
        dto.setToolCode("");
        dto.setOperation("upscale");

        assertThrows(IllegalArgumentException.class, () -> service.createPostProcessTask(dto));
    }

    @Test
    void createTaskRejectsUnsupportedOperation() {
        RecordingMapper mapper = new RecordingMapper(null);
        PostProcessTaskServiceImpl service = service(mapper, toolService(false, null));

        PostProcessTaskCreateDTO dto = new PostProcessTaskCreateDTO();
        dto.setSourceImagePath("exports/test.png");
        dto.setToolCode("real-esrgan");
        dto.setOperation("invalid-op");

        assertThrows(IllegalArgumentException.class, () -> service.createPostProcessTask(dto));
    }

    @Test
    void retryRejectsRunningTasks() {
        PostProcessTask task = new PostProcessTask();
        task.setId(600L);
        task.setToolCode("real-esrgan");
        task.setOperation("upscale");
        task.setStatus("RUNNING");

        RecordingMapper mapper = new RecordingMapper(task);
        PostProcessTaskServiceImpl service = service(mapper, toolService(true, null));

        assertThrows(IllegalStateException.class, () -> service.retryPostProcessTask(600L));
    }

    @Test
    void retryRejectsSucceededTasks() {
        PostProcessTask task = new PostProcessTask();
        task.setId(601L);
        task.setToolCode("real-esrgan");
        task.setOperation("upscale");
        task.setStatus("SUCCEEDED");

        RecordingMapper mapper = new RecordingMapper(task);
        PostProcessTaskServiceImpl service = service(mapper, toolService(true, null));

        assertThrows(IllegalStateException.class, () -> service.retryPostProcessTask(601L));
    }

    @Test
    void cancelRejectsTerminalTasks() {
        PostProcessTask task = new PostProcessTask();
        task.setId(602L);
        task.setToolCode("real-esrgan");
        task.setOperation("upscale");
        task.setStatus("SUCCEEDED");

        RecordingMapper mapper = new RecordingMapper(task);
        PostProcessTaskServiceImpl service = service(mapper, toolService(true, null));

        assertThrows(IllegalStateException.class, () -> service.cancelPostProcessTask(602L));
    }

    @Test
    void getTaskByIdReturnsStoredFields() {
        PostProcessTask task = new PostProcessTask();
        task.setId(603L);
        task.setToolCode("iopaint");
        task.setOperation("inpaint");
        task.setStatus("SUCCEEDED");
        task.setSourceImagePath("exports/input.png");
        task.setOutputImagePath("exports/output.png");
        task.setParamsJson("{\"brush\":\"soft\"}");

        RecordingMapper mapper = new RecordingMapper(task);
        PostProcessTaskServiceImpl service = service(mapper, toolService(false, null));

        PostProcessTaskDTO dto = service.getPostProcessTaskById(603L);

        assertEquals(603L, dto.getId());
        assertEquals("iopaint", dto.getToolCode());
        assertEquals("inpaint", dto.getOperation());
        assertEquals("SUCCEEDED", dto.getStatus());
        assertNotNull(dto.getParams());
        assertEquals("soft", dto.getParams().get("brush"));
    }

    @Test
    void getTaskByIdThrowsWhenNotFound() {
        RecordingMapper mapper = new RecordingMapper(null);
        PostProcessTaskServiceImpl service = service(mapper, toolService(false, null));

        assertThrows(RuntimeException.class, () -> service.getPostProcessTaskById(999L));
    }

    @Test
    void cancelRunningTaskSucceeds() {
        PostProcessTask task = new PostProcessTask();
        task.setId(604L);
        task.setToolCode("real-esrgan");
        task.setOperation("upscale");
        task.setStatus("RUNNING");

        RecordingMapper mapper = new RecordingMapper(task);
        PostProcessTaskServiceImpl service = service(mapper, toolService(true, null));

        boolean result = service.cancelPostProcessTask(604L);

        assertTrue(result);
        assertEquals("CANCELED", mapper.updatedTask.getStatus());
    }

    // ©¤©¤ Helpers ©¤©¤

    private static PostProcessTaskServiceImpl service(RecordingMapper mapper, ToolAdapterService toolAdapterService) {
        PostProcessTaskServiceImpl svc = new PostProcessTaskServiceImpl();
        ReflectionTestUtils.setField(svc, "baseMapper", mapper.proxy());
        ReflectionTestUtils.setField(svc, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(svc, "toolAdapterService", toolAdapterService);
        return svc;
    }

    private static ToolAdapterService toolService(boolean configured, ToolInvokeResponseDTO invokeResponse) {
        return (ToolAdapterService) Proxy.newProxyInstance(
                ToolAdapterService.class.getClassLoader(),
                new Class<?>[]{ToolAdapterService.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getTool" -> new ToolAdapterInfoDTO(
                            String.valueOf(args[0]),
                            "TestTool",
                            "IMAGE_ENHANCE",
                            "https://github.com/test/test",
                            1, "MIT",
                            "SELF_HOSTED_HTTP_SERVICE",
                            "Test tool",
                            "upscale",
                            "/upscale",
                            List.of("upscale"),
                            configured,
                            configured ? "CONFIGURED" : "NOT_CONFIGURED");
                    case "invoke" -> {
                        if (invokeResponse == null) {
                            throw new UnsupportedOperationException("tool unavailable");
                        }
                        yield invokeResponse;
                    }
                    case "listTools" -> List.of();
                    default -> null;
                });
    }

    private static class RecordingMapper {
        private final PostProcessTask storedTask;
        PostProcessTask insertedTask;
        PostProcessTask updatedTask;
        int insertCount;
        int updateCount;

        RecordingMapper(PostProcessTask storedTask) {
            this.storedTask = storedTask;
        }

        PostProcessTaskMapper proxy() {
            return (PostProcessTaskMapper) Proxy.newProxyInstance(
                    PostProcessTaskMapper.class.getClassLoader(),
                    new Class<?>[]{PostProcessTaskMapper.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "selectById" -> {
                            Serializable id = (Serializable) args[0];
                            yield storedTask != null && storedTask.getId().equals(id) ? storedTask : null;
                        }
                        case "insert" -> {
                            insertedTask = (PostProcessTask) args[0];
                            insertedTask.setId(501L);
                            insertCount++;
                            yield 1;
                        }
                        case "updateById" -> {
                            updatedTask = (PostProcessTask) args[0];
                            updateCount++;
                            yield 1;
                        }
                        default -> 0;
                    });
        }
    }
}
