package com.ecommerce.detail.ai.service.impl;

import com.ecommerce.detail.ai.dto.ImageJobCreateDTO;
import com.ecommerce.detail.ai.dto.ImageJobDTO;
import com.ecommerce.detail.ai.dto.ImageJobRetryDTO;
import com.ecommerce.detail.ai.dto.ImageJobStatusDTO;
import com.ecommerce.detail.ai.dto.tool.ToolAdapterInfoDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeResponseDTO;
import com.ecommerce.detail.ai.entity.ImageJob;
import com.ecommerce.detail.ai.mapper.ImageJobMapper;
import com.ecommerce.detail.ai.service.ToolAdapterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageJobServiceImplTest {

    @Test
    void createImageJobCancelsWhenToolIsNotConfigured() {
        RecordingImageJobMapper mapper = new RecordingImageJobMapper(null);
        ImageJobServiceImpl service = service(mapper, toolService(false, null));

        ImageJobCreateDTO dto = new ImageJobCreateDTO();
        dto.setTaskName("generate-cover");
        dto.setToolCode("comfyui");
        dto.setInputData(Map.of("prompt", "a product banner"));

        Long id = service.createImageJob(dto);

        assertEquals(301L, id);
        assertEquals(1, mapper.insertCount);
        assertEquals(0, mapper.updateCount);
        assertEquals("CANCELED", mapper.insertedJob.getStatus());
        assertTrue(mapper.insertedJob.getErrorMessage().contains("not configured"));
    }

    @Test
    void createImageJobPersistsExternalJobIdWhenToolIsConfigured() {
        RecordingImageJobMapper mapper = new RecordingImageJobMapper(null);
        ToolInvokeResponseDTO invokeResponse = new ToolInvokeResponseDTO(
                "comfyui",
                "image-generate",
                200,
                Map.of("prompt_id", "prompt-123"),
                "{\"prompt_id\":\"prompt-123\"}");
        ImageJobServiceImpl service = service(mapper, toolService(true, invokeResponse));

        ImageJobCreateDTO dto = new ImageJobCreateDTO();
        dto.setTaskName("generate-detail");
        dto.setToolCode("comfyui");
        dto.setInputData(Map.of("workflow", "simple"));

        Long id = service.createImageJob(dto);

        assertEquals(301L, id);
        assertEquals(1, mapper.insertCount);
        assertEquals(0, mapper.updateCount);
        assertEquals("RUNNING", mapper.insertedJob.getStatus());
        assertEquals("prompt-123", mapper.insertedJob.getExternalJobId());
        assertEquals(0, mapper.insertedJob.getProgress());
        assertFalse(mapper.insertedJob.getInputJson().isBlank());
    }

    @Test
    void retryImageJobRejectsActiveJobs() {
        ImageJob job = new ImageJob();
        job.setId(400L);
        job.setTaskName("retry-me");
        job.setToolCode("comfyui");
        job.setStatus("RUNNING");

        RecordingImageJobMapper mapper = new RecordingImageJobMapper(job);
        ImageJobServiceImpl service = service(mapper, toolService(true, null));

        assertThrows(IllegalStateException.class, () -> service.retryImageJob(400L, new ImageJobRetryDTO()));
    }

    @Test
    void retryImageJobRejectsSucceededJobs() {
        ImageJob job = new ImageJob();
        job.setId(401L);
        job.setTaskName("done-job");
        job.setToolCode("comfyui");
        job.setStatus("SUCCEEDED");

        RecordingImageJobMapper mapper = new RecordingImageJobMapper(job);
        ImageJobServiceImpl service = service(mapper, toolService(true, null));

        assertThrows(IllegalStateException.class, () -> service.retryImageJob(401L, new ImageJobRetryDTO()));
    }

    @Test
    void updateImageJobStatusRejectsUnsupportedStatus() {
        ImageJob job = new ImageJob();
        job.setId(402L);
        job.setTaskName("status-job");
        job.setToolCode("comfyui");
        job.setStatus("PENDING");

        RecordingImageJobMapper mapper = new RecordingImageJobMapper(job);
        ImageJobServiceImpl service = service(mapper, toolService(false, null));

        ImageJobStatusDTO dto = new ImageJobStatusDTO();
        dto.setStatus("done");

        assertThrows(IllegalArgumentException.class, () -> service.updateImageJobStatus(402L, dto));
    }

    @Test
    void updateImageJobStatusRejectsTerminalReopen() {
        ImageJob job = new ImageJob();
        job.setId(404L);
        job.setTaskName("terminal-job");
        job.setToolCode("comfyui");
        job.setStatus("SUCCEEDED");

        RecordingImageJobMapper mapper = new RecordingImageJobMapper(job);
        ImageJobServiceImpl service = service(mapper, toolService(false, null));

        ImageJobStatusDTO dto = new ImageJobStatusDTO();
        dto.setStatus("RUNNING");

        assertThrows(IllegalStateException.class, () -> service.updateImageJobStatus(404L, dto));
        assertEquals(0, mapper.updateCount);
    }

    @Test
    void cancelImageJobRejectsTerminalJobs() {
        ImageJob job = new ImageJob();
        job.setId(405L);
        job.setTaskName("terminal-cancel");
        job.setToolCode("comfyui");
        job.setStatus("FAILED");

        RecordingImageJobMapper mapper = new RecordingImageJobMapper(job);
        ImageJobServiceImpl service = service(mapper, toolService(false, null));

        assertThrows(IllegalStateException.class, () -> service.cancelImageJob(405L, new ImageJobStatusDTO()));
        assertEquals(0, mapper.updateCount);
    }

    @Test
    void cancelImageJobAllowsRunningJobs() {
        ImageJob job = new ImageJob();
        job.setId(406L);
        job.setTaskName("running-cancel");
        job.setToolCode("comfyui");
        job.setStatus("RUNNING");
        job.setProgress(24);

        RecordingImageJobMapper mapper = new RecordingImageJobMapper(job);
        ImageJobServiceImpl service = service(mapper, toolService(false, null));

        ImageJobStatusDTO dto = new ImageJobStatusDTO();
        dto.setErrorMessage("user canceled");

        assertTrue(service.cancelImageJob(406L, dto));
        assertEquals(1, mapper.updateCount);
        assertEquals("CANCELED", mapper.updatedJob.getStatus());
        assertEquals(24, mapper.updatedJob.getProgress());
        assertEquals("user canceled", mapper.updatedJob.getErrorMessage());
    }

    @Test
    void getImageJobByIdReturnsStoredFields() {
        ImageJob job = new ImageJob();
        job.setId(403L);
        job.setTaskName("stored-job");
        job.setToolCode("comfyui");
        job.setStatus("CANCELED");
        job.setInputJson("{\"prompt\":\"hello\"}");

        RecordingImageJobMapper mapper = new RecordingImageJobMapper(job);
        ImageJobServiceImpl service = service(mapper, toolService(false, null));

        ImageJobDTO dto = service.getImageJobById(403L);

        assertEquals(403L, dto.getId());
        assertEquals("stored-job", dto.getTaskName());
        assertEquals("comfyui", dto.getToolCode());
        assertNotNull(dto.getInputData());
        assertEquals("hello", dto.getInputData().get("prompt"));
    }

    private static ImageJobServiceImpl service(RecordingImageJobMapper mapper, ToolAdapterService toolAdapterService) {
        ImageJobServiceImpl service = new ImageJobServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper.proxy());
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(service, "toolAdapterService", toolAdapterService);
        return service;
    }

    private static ToolAdapterService toolService(boolean configured, ToolInvokeResponseDTO invokeResponse) {
        return (ToolAdapterService) Proxy.newProxyInstance(
                ToolAdapterService.class.getClassLoader(),
                new Class<?>[]{ToolAdapterService.class},
                (proxy, method, args) -> {
                    return switch (method.getName()) {
                        case "getTool" -> new ToolAdapterInfoDTO(
                                String.valueOf(args[0]),
                                "ComfyUI",
                                "IMAGE_GENERATION",
                                "https://github.com/Comfy-Org/ComfyUI",
                                1,
                                "GPL-3.0",
                                "SELF_HOSTED_HTTP_SERVICE",
                                "Standalone image generation",
                                "image-generate",
                                "/prompt",
                                List.of("image-generate"),
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
                    };
                });
    }

    private static class RecordingImageJobMapper {
        private final ImageJob storedJob;
        private ImageJob insertedJob;
        private ImageJob updatedJob;
        private int insertCount;
        private int updateCount;

        private RecordingImageJobMapper(ImageJob storedJob) {
            this.storedJob = storedJob;
        }

        private ImageJobMapper proxy() {
            return (ImageJobMapper) Proxy.newProxyInstance(
                    ImageJobMapper.class.getClassLoader(),
                    new Class<?>[]{ImageJobMapper.class},
                    (proxy, method, args) -> {
                        return switch (method.getName()) {
                            case "selectById" -> {
                                Serializable id = (Serializable) args[0];
                                yield storedJob != null && storedJob.getId().equals(id) ? storedJob : null;
                            }
                            case "insert" -> {
                                insertedJob = (ImageJob) args[0];
                                insertedJob.setId(301L);
                                insertCount++;
                                yield 1;
                            }
                            case "updateById" -> {
                                updatedJob = (ImageJob) args[0];
                                updateCount++;
                                yield 1;
                            }
                            default -> 0;
                        };
                    });
        }
    }
}
