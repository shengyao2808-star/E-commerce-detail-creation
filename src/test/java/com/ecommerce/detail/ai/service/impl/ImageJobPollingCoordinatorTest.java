package com.ecommerce.detail.ai.service.impl;

import com.ecommerce.detail.ai.dto.GenerationResultDTO;
import com.ecommerce.detail.ai.dto.tool.ToolAdapterInfoDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeResponseDTO;
import com.ecommerce.detail.ai.entity.ImageJob;
import com.ecommerce.detail.ai.mapper.ImageJobMapper;
import com.ecommerce.detail.ai.service.GenerationResultService;
import com.ecommerce.detail.ai.service.ToolAdapterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageJobPollingCoordinatorTest {

    @Test
    void pollImageJobsMarksJobSucceededAfterPersistingRealResults() {
        ImageJob job = runningJob(700L);
        RecordingImageJobMapper mapper = new RecordingImageJobMapper(job);
        RecordingGenerationResultService generationResults = new RecordingGenerationResultService();
        ImageJobPollingCoordinator coordinator = coordinator(
                mapper,
                generationResults.proxy(),
                toolService(true, "http://127.0.0.1:8188", historyResponse(
                        Map.of(
                                "prompt-123", Map.of(
                                        "status", Map.of(
                                                "status_str", "success",
                                                "completed", true
                                        ),
                                        "execution_success", true,
                                        "outputs", Map.of(
                                                "9", Map.of(
                                                        "images", List.of(Map.of(
                                                                "filename", "result.png",
                                                                "subfolder", "",
                                                                "type", "output"
                                                        ))
                                                ),
                                                "10", Map.of(
                                                        "images", List.of(Map.of(
                                                                "filename", "result.png",
                                                                "subfolder", "",
                                                                "type", "output",
                                                                "thumbnailUrl", "/thumb/result.png"
                                                        ))
                                                )
                                        )
                                )
                        )
                ))
        );

        coordinator.pollImageJobs();

        assertEquals(1, mapper.updateCount);
        assertEquals("SUCCEEDED", mapper.updatedJob.getStatus());
        assertEquals(100, mapper.updatedJob.getProgress());
        assertEquals(1, generationResults.upsertCount);
        assertEquals(1, generationResults.savedResults.size());
        assertEquals(
                "http://127.0.0.1:8188/view?filename=result.png&subfolder=&type=output",
                generationResults.savedResults.get(0).getResultUrl()
        );
    }

    @Test
    void pollVisualPlanBatchesPollsPlanJobsAndReturnsAffectedPlans() {
        ImageJob job = runningJob(706L);
        job.setVisualPlanId(880L);
        RecordingImageJobMapper mapper = new RecordingImageJobMapper(job);
        RecordingGenerationResultService generationResults = new RecordingGenerationResultService();
        ImageJobPollingCoordinator coordinator = coordinator(
                mapper,
                generationResults.proxy(),
                toolService(true, "http://127.0.0.1:8188", historyResponse(
                        Map.of(
                                "prompt-123", Map.of(
                                        "status", Map.of(
                                                "status_str", "success",
                                                "completed", true
                                        ),
                                        "outputs", Map.of(
                                                "9", Map.of(
                                                        "images", List.of(Map.of(
                                                                "filename", "slot-a.png",
                                                                "subfolder", "",
                                                                "type", "output"
                                                        ))
                                                )
                                        )
                                )
                        )
                ))
        );

        var affectedPlans = coordinator.pollVisualPlanBatches();

        assertTrue(affectedPlans.contains(880L));
        assertEquals("SUCCEEDED", mapper.updatedJob.getStatus());
        assertEquals(1, generationResults.upsertCount);
    }

    @Test
    void pollImageJobsFailsWhenHistoryContainsNoOutput() {
        ImageJob job = runningJob(701L);
        RecordingImageJobMapper mapper = new RecordingImageJobMapper(job);
        ImageJobPollingCoordinator coordinator = coordinator(
                mapper,
                new RecordingGenerationResultService().proxy(),
                toolService(true, "http://127.0.0.1:8188", historyResponse(
                        Map.of(
                                "prompt-123", Map.of(
                                        "status", Map.of(
                                                "status_str", "success",
                                                "completed", true
                                        ),
                                        "execution_success", true,
                                        "outputs", Map.of()
                                )
                        )
                ))
        );

        coordinator.pollImageJobs();

        assertEquals("FAILED", mapper.updatedJob.getStatus());
        assertTrue(mapper.updatedJob.getErrorMessage().contains("without output"));
    }

    @Test
    void pollImageJobsCancelsWhenToolIsNotConfigured() {
        ImageJob job = runningJob(702L);
        RecordingImageJobMapper mapper = new RecordingImageJobMapper(job);
        ImageJobPollingCoordinator coordinator = coordinator(
                mapper,
                new RecordingGenerationResultService().proxy(),
                toolService(false, "", null)
        );

        coordinator.pollImageJobs();

        assertEquals("CANCELED", mapper.updatedJob.getStatus());
        assertTrue(mapper.updatedJob.getErrorMessage().contains("not configured"));
    }

    @Test
    void pollImageJobsKeepsRunningJobsAndUpdatesRealProgress() {
        ImageJob job = runningJob(703L);
        RecordingImageJobMapper mapper = new RecordingImageJobMapper(job);
        ImageJobPollingCoordinator coordinator = coordinator(
                mapper,
                new RecordingGenerationResultService().proxy(),
                toolService(true, "http://127.0.0.1:8188", historyResponse(
                        Map.of(
                                "prompt-123", Map.of(
                                        "status", Map.of(
                                                "status_str", "running",
                                                "completed", false,
                                                "progress", 35
                                        ),
                                        "outputs", Map.of()
                                )
                        )
                ))
        );

        coordinator.pollImageJobs();

        assertEquals("RUNNING", mapper.updatedJob.getStatus());
        assertEquals(35, mapper.updatedJob.getProgress());
    }

    @Test
    void pollImageJobsFailsClosedOnMalformedHistoryPayload() {
        ImageJob job = runningJob(704L);
        RecordingImageJobMapper mapper = new RecordingImageJobMapper(job);
        ImageJobPollingCoordinator coordinator = coordinator(
                mapper,
                new RecordingGenerationResultService().proxy(),
                toolService(true, "http://127.0.0.1:8188", historyResponse(
                        Map.of("prompt-123", Map.of("outputs", Map.of()))
                ))
        );

        coordinator.pollImageJobs();

        assertEquals("FAILED", mapper.updatedJob.getStatus());
        assertTrue(mapper.updatedJob.getErrorMessage().contains("malformed"));
    }

    @Test
    void pollSingleJobDoesNotReopenTerminalJobs() {
        ImageJob job = runningJob(705L);
        job.setStatus("SUCCEEDED");
        RecordingImageJobMapper mapper = new RecordingImageJobMapper(job);
        ImageJobPollingCoordinator coordinator = coordinator(
                mapper,
                new RecordingGenerationResultService().proxy(),
                toolService(true, "http://127.0.0.1:8188", historyResponse(Map.of()))
        );

        coordinator.pollSingleJob(705L);

        assertEquals(0, mapper.updateCount);
    }

    private static ImageJobPollingCoordinator coordinator(
            RecordingImageJobMapper mapper,
            GenerationResultService generationResultService,
            ToolAdapterService toolAdapterService
    ) {
        ImageJobPollingCoordinator coordinator = new ImageJobPollingCoordinator();
        ReflectionTestUtils.setField(coordinator, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(coordinator, "imageJobMapper", mapper.proxy());
        ReflectionTestUtils.setField(coordinator, "generationResultService", generationResultService);
        ReflectionTestUtils.setField(coordinator, "toolAdapterService", toolAdapterService);
        return coordinator;
    }

    private static ImageJob runningJob(Long id) {
        ImageJob job = new ImageJob();
        job.setId(id);
        job.setTaskName("job-" + id);
        job.setToolCode("comfyui");
        job.setStatus("RUNNING");
        job.setProgress(0);
        job.setExternalJobId("prompt-123");
        job.setInputJson("{\"prompt\":\"product hero\"}");
        return job;
    }

    private static ToolInvokeResponseDTO historyResponse(Map<String, Object> body) {
        return new ToolInvokeResponseDTO("comfyui", "history", 200, body, body.toString());
    }

    private static ToolAdapterService toolService(boolean configured, String baseUrl, ToolInvokeResponseDTO response) {
        return (ToolAdapterService) Proxy.newProxyInstance(
                ToolAdapterService.class.getClassLoader(),
                new Class<?>[]{ToolAdapterService.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getTool" -> new ToolAdapterInfoDTO(
                            "comfyui",
                            "ComfyUI",
                            "IMAGE_GENERATION",
                            "https://github.com/Comfy-Org/ComfyUI",
                            1,
                            "GPL-3.0",
                            "SELF_HOSTED_HTTP_SERVICE",
                            "Standalone image generation",
                            "image-generate",
                            "/prompt",
                            List.of("image-generate", "history"),
                            configured,
                            configured ? "CONFIGURED" : "NOT_CONFIGURED");
                    case "getBaseUrl" -> baseUrl;
                    case "invoke" -> {
                        if (response == null) {
                            throw new IllegalStateException("history unavailable");
                        }
                        yield response;
                    }
                    case "listTools" -> List.of();
                    default -> null;
                });
    }

    private static class RecordingImageJobMapper {
        private final ImageJob storedJob;
        private ImageJob updatedJob;
        private int updateCount;

        private RecordingImageJobMapper(ImageJob storedJob) {
            this.storedJob = storedJob;
        }

        private ImageJobMapper proxy() {
            return (ImageJobMapper) Proxy.newProxyInstance(
                    ImageJobMapper.class.getClassLoader(),
                    new Class<?>[]{ImageJobMapper.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "selectList" -> List.of(storedJob);
                        case "selectById" -> {
                            Serializable id = (Serializable) args[0];
                            yield storedJob != null && storedJob.getId().equals(id) ? storedJob : null;
                        }
                        case "updateById" -> {
                            updatedJob = (ImageJob) args[0];
                            updateCount++;
                            yield 1;
                        }
                        default -> 0;
                    });
        }
    }

    private static class RecordingGenerationResultService {
        private final List<GenerationResultDTO> savedResults = new ArrayList<>();
        private int upsertCount;

        private GenerationResultService proxy() {
            return (GenerationResultService) Proxy.newProxyInstance(
                    GenerationResultService.class.getClassLoader(),
                    new Class<?>[]{GenerationResultService.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "upsertGenerationResult", "saveGenerationResult" -> {
                            GenerationResultDTO dto = (GenerationResultDTO) args[0];
                            savedResults.add(dto);
                            upsertCount++;
                            yield 900L + upsertCount;
                        }
                        case "listGenerationResults" -> null;
                        case "getGenerationResultById" -> null;
                        case "updateGenerationResultSelection" -> true;
                        default -> null;
                    });
        }
    }
}
