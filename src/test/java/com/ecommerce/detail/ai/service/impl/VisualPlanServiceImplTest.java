package com.ecommerce.detail.ai.service.impl;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.CategoryVisualPolicyDTO;
import com.ecommerce.detail.ai.dto.ModelProfileDTO;
import com.ecommerce.detail.ai.dto.PromptWorkbenchEntryDTO;
import com.ecommerce.detail.ai.dto.SkcPolicyDTO;
import com.ecommerce.detail.ai.dto.VisualPlanConfirmDTO;
import com.ecommerce.detail.ai.dto.GenerationResultDTO;
import com.ecommerce.detail.ai.dto.GenerationResultListQuery;
import com.ecommerce.detail.ai.dto.GenerationResultSelectionDTO;
import com.ecommerce.detail.ai.dto.ImageJobCreateDTO;
import com.ecommerce.detail.ai.dto.ImageJobDTO;
import com.ecommerce.detail.ai.dto.VisualPlanBatchStatusDTO;
import com.ecommerce.detail.ai.dto.VisualPlanDTO;
import com.ecommerce.detail.ai.service.GenerationResultService;
import com.ecommerce.detail.ai.service.ImageJobService;
import com.ecommerce.detail.ai.entity.VisualPlan;
import com.ecommerce.detail.ai.mapper.VisualPlanMapper;
import com.ecommerce.detail.ai.service.CategoryVisualPolicyService;
import com.ecommerce.detail.ai.service.ModelProfileService;
import com.ecommerce.detail.ai.service.PromptWorkbenchService;
import com.ecommerce.detail.ai.service.SkcPolicyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VisualPlanServiceImplTest {

    @Test
    void createAndConfirmPlanPersistsSnapshotAndIsIdempotent() {
        RecordingVisualPlanMapper mapper = new RecordingVisualPlanMapper(null);
        VisualPlanServiceImpl service = service(mapper, catalogServices());

        VisualPlanDTO dto = new VisualPlanDTO();
        dto.setProductDetailId(701L);
        dto.setPlanName("濂宠瑙嗚瑙勫垝");
        dto.setCategoryCode("women-dress");
        dto.setCategoryVisualPolicyId(801L);
        dto.setModelProfileId(901L);
        dto.setSkcPolicyId(1001L);
        dto.setPromptWorkbenchEntryIds(List.of(1101L, 1102L));
        dto.setInputData(Map.of("brand", "demo"));
        dto.setPlanData(Map.of("heroImages", 5));
        dto.setStatus("CONFIRMED");
        dto.setVersion(99);

        Long id = service.createVisualPlan(dto);
        assertEquals(701L, id);
        assertEquals("DRAFT", mapper.current.getStatus());
        assertEquals(1, mapper.current.getVersion());
        assertEquals("[1101,1102]", mapper.current.getPromptWorkbenchEntryIdsJson());

        VisualPlan stored = mapper.current;
        stored.setId(701L);
        mapper = new RecordingVisualPlanMapper(stored);
        service = service(mapper, catalogServices());

        VisualPlanConfirmDTO confirmDTO = new VisualPlanConfirmDTO();
        confirmDTO.setConfirmData(Map.of("auditor", "system"));
        VisualPlanDTO confirmed = service.confirmVisualPlan(701L, confirmDTO);

        assertEquals("CONFIRMED", confirmed.getStatus());
        assertEquals(2, confirmed.getVersion());
        assertEquals("DRAFT", ((Map<?, ?>) confirmed.getSnapshotData().get("visualPlan")).get("status"));
        @SuppressWarnings("unchecked")
        Map<String, Object> visualPlan = (Map<String, Object>) confirmed.getSnapshotData().get("visualPlan");
        @SuppressWarnings("unchecked")
        Map<String, Object> categoryPolicy = (Map<String, Object>) confirmed.getSnapshotData().get("categoryVisualPolicy");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> promptEntries = (List<Map<String, Object>>) confirmed.getSnapshotData().get("promptWorkbenchEntries");
        assertEquals("women-dress", visualPlan.get("categoryCode"));
        assertEquals("category-801", categoryPolicy.get("categoryCode"));
        assertEquals(2, promptEntries.size());
        assertEquals("GUIDED", promptEntries.get(0).get("entryType"));
        assertEquals("SUCCEEDED", promptEntries.get(0).get("status"));
        assertEquals(1, promptEntries.get(0).get("version"));

        VisualPlanDTO confirmedAgain = service.confirmVisualPlan(701L, confirmDTO);
        assertEquals("CONFIRMED", confirmedAgain.getStatus());
        assertEquals(2, confirmedAgain.getVersion());
    }

    @Test
    void createPlanRejectsBlankName() {
        VisualPlanServiceImpl service = service(new RecordingVisualPlanMapper(null), catalogServices());
        VisualPlanDTO dto = new VisualPlanDTO();
        assertThrows(IllegalArgumentException.class, () -> service.createVisualPlan(dto));
    }

    @Test
    void updateVisualPlanAllowedInDraftStatus() {
        VisualPlan stored = new VisualPlan();
        stored.setId(701L);
        stored.setPlanName("draft-plan");
        stored.setStatus("DRAFT");
        stored.setVersion(1);
        stored.setCreateTime(java.time.LocalDateTime.now());
        stored.setUpdateTime(java.time.LocalDateTime.now());
        RecordingVisualPlanMapper mapper = new RecordingVisualPlanMapper(stored);
        VisualPlanServiceImpl service = service(mapper, catalogServices());

        VisualPlanDTO update = new VisualPlanDTO();
        update.setPlanName("updated-plan");
        boolean result = service.updateVisualPlan(701L, update);
        assertEquals(true, result);
        assertEquals("updated-plan", mapper.current.getPlanName());
        assertEquals(2, mapper.current.getVersion());
    }

    @Test
    void updateVisualPlanRejectsConfirmedPlan() {
        VisualPlan stored = new VisualPlan();
        stored.setId(701L);
        stored.setPlanName("confirmed-plan");
        stored.setStatus("CONFIRMED");
        stored.setVersion(2);
        stored.setCreateTime(java.time.LocalDateTime.now());
        stored.setUpdateTime(java.time.LocalDateTime.now());
        RecordingVisualPlanMapper mapper = new RecordingVisualPlanMapper(stored);
        VisualPlanServiceImpl service = service(mapper, catalogServices());

        VisualPlanDTO update = new VisualPlanDTO();
        update.setPlanName("should-not-update");
        assertThrows(IllegalStateException.class, () -> service.updateVisualPlan(701L, update));
    }

    @Test
    void confirmVisualPlanRejectsUnconfirmedCategoryPolicy() {
        VisualPlan stored = new VisualPlan();
        stored.setId(701L);
        stored.setPlanName("draft-plan");
        stored.setCategoryVisualPolicyId(801L);
        stored.setStatus("DRAFT");
        stored.setVersion(1);
        stored.setCreateTime(java.time.LocalDateTime.now());
        stored.setUpdateTime(java.time.LocalDateTime.now());
        RecordingVisualPlanMapper mapper = new RecordingVisualPlanMapper(stored);
        VisualPlanServiceImpl service = service(mapper, catalogServicesWithUnconfirmedPolicy());

        VisualPlanConfirmDTO confirmDTO = new VisualPlanConfirmDTO();
        assertThrows(IllegalStateException.class, () -> service.confirmVisualPlan(701L, confirmDTO));
    }


    @Test
    void dispatchFromDraftPlanThrows() {
        VisualPlan stored = new VisualPlan();
        stored.setId(701L);
        stored.setPlanName("draft-plan");
        stored.setStatus("DRAFT");
        stored.setVersion(1);
        stored.setCreateTime(java.time.LocalDateTime.now());
        stored.setUpdateTime(java.time.LocalDateTime.now());
        RecordingVisualPlanMapper mapper = new RecordingVisualPlanMapper(stored);
        VisualPlanServiceImpl service = service(mapper, catalogServices(), null);

        List<ImageJobCreateDTO> jobs = List.of(buildJobCreate("cover", "main"));
        assertThrows(IllegalStateException.class, () -> service.dispatchFromVisualPlan(701L, jobs));
    }

    @Test
    void dispatchFromConfirmedPlanSetsTraceabilityFields() {
        VisualPlan stored = new VisualPlan();
        stored.setId(701L);
        stored.setPlanName("confirmed-plan");
        stored.setStatus("CONFIRMED");
        stored.setModelProfileId(901L);
        stored.setConfirmedSnapshotJson("{\"visualPlan\":{\"id\":701}}");
        stored.setVersion(2);
        stored.setCreateTime(java.time.LocalDateTime.now());
        stored.setUpdateTime(java.time.LocalDateTime.now());
        RecordingVisualPlanMapper mapper = new RecordingVisualPlanMapper(stored);

        RecordingImageJobService imageJobService = new RecordingImageJobService();
        VisualPlanServiceImpl service = service(mapper, catalogServices(), imageJobService);

        ImageJobCreateDTO job = buildJobCreate("cover", "main");
        List<Long> ids = service.dispatchFromVisualPlan(701L, List.of(job));
        assertEquals(1, ids.size());
        assertEquals(501L, ids.get(0));

        ImageJobCreateDTO captured = imageJobService.lastCreate;
        assertEquals(701L, captured.getVisualPlanId());
        assertEquals(901L, captured.getModelProfileId());
        assertEquals("{\"visualPlan\":{\"id\":701}}", captured.getSourceSnapshotJson());
    }

    @Test
    void getBatchStatusAggregatesJobStates() {
        VisualPlan stored = new VisualPlan();
        stored.setId(701L);
        stored.setPlanName("batch-plan");
        stored.setStatus("CONFIRMED");
        stored.setVersion(2);
        stored.setCreateTime(java.time.LocalDateTime.now());
        stored.setUpdateTime(java.time.LocalDateTime.now());
        RecordingVisualPlanMapper mapper = new RecordingVisualPlanMapper(stored);

        RecordingImageJobService imageJobService = new RecordingImageJobService();
        imageJobService.jobsForPlan = List.of(
                buildJobDTO(501L, "cover", "SUCCEEDED", "main", "16:9"),
                buildJobDTO(502L, "detail", "CANCELED", "detail-screen-1", "3:4"),
                buildJobDTO(503L, "hero", "FAILED", "hero", "1:1")
        );
        VisualPlanServiceImpl service = service(mapper, catalogServices(), imageJobService);

        VisualPlanBatchStatusDTO batch = service.getBatchStatus(701L);
        assertEquals(701L, batch.getVisualPlanId());
        assertEquals(3, batch.getTotalJobs());
        assertEquals(1, batch.getSucceededJobs());
        assertEquals(0, batch.getPendingJobs());
        assertEquals(1, batch.getCanceledJobs());
        assertEquals(1, batch.getFailedJobs());
        assertEquals("PARTIAL_SUCCEEDED", batch.getAggregatedStatus());
        assertEquals(3, batch.getJobSummaries().size());
    }

    @Test
    void dispatchRejectsEmptyJobList() {
        VisualPlan stored = new VisualPlan();
        stored.setId(701L);
        stored.setPlanName("confirmed-plan");
        stored.setStatus("CONFIRMED");
        stored.setVersion(2);
        stored.setCreateTime(java.time.LocalDateTime.now());
        stored.setUpdateTime(java.time.LocalDateTime.now());
        RecordingVisualPlanMapper mapper = new RecordingVisualPlanMapper(stored);
        VisualPlanServiceImpl service = service(mapper, catalogServices(), new RecordingImageJobService());

        assertThrows(IllegalArgumentException.class, () -> service.dispatchFromVisualPlan(701L, List.of()));
        assertThrows(IllegalArgumentException.class, () -> service.dispatchFromVisualPlan(701L, null));
    }

    @Test
    void getBatchResultsBySlotIncludesPersistedGenerationResults() {
        VisualPlan stored = new VisualPlan();
        stored.setId(701L);
        stored.setPlanName("batch-plan");
        stored.setStatus("CONFIRMED");
        stored.setVersion(2);
        stored.setCreateTime(java.time.LocalDateTime.now());
        stored.setUpdateTime(java.time.LocalDateTime.now());
        RecordingVisualPlanMapper mapper = new RecordingVisualPlanMapper(stored);

        RecordingImageJobService imageJobService = new RecordingImageJobService();
        imageJobService.jobsForPlan = List.of(
                buildJobDTO(501L, "cover", "SUCCEEDED", "main", "1:1"),
                buildJobDTO(502L, "detail", "SUCCEEDED", "detail", "3:4")
        );
        RecordingGenerationResultService generationResultService = new RecordingGenerationResultService();
        generationResultService.resultsForJob = Map.of(
                501L, List.of(buildGenerationResult(901L, 501L, "exports/main.png")),
                502L, List.of(buildGenerationResult(902L, 502L, "exports/detail.png"))
        );
        VisualPlanServiceImpl service = service(mapper, catalogServices(), imageJobService, generationResultService);

        Map<String, Object> grouped = service.getBatchResultsBySlot(701L, "main");
        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, Object>>> slotGroups =
                (Map<String, List<Map<String, Object>>>) grouped.get("slotGroups");
        assertEquals(1, slotGroups.size());
        assertEquals(true, slotGroups.containsKey("main"));
        @SuppressWarnings("unchecked")
        List<GenerationResultDTO> results = (List<GenerationResultDTO>) slotGroups.get("main").get(0).get("results");
        assertEquals(1, results.size());
        assertEquals("exports/main.png", results.get(0).getResultUrl());
    }

    private ImageJobCreateDTO buildJobCreate(String taskName, String slot) {
        ImageJobCreateDTO dto = new ImageJobCreateDTO();
        dto.setTaskName(taskName);
        dto.setToolCode("comfyui");
        dto.setSlot(slot);
        dto.setRatio("16:9");
        return dto;
    }

    private ImageJobDTO buildJobDTO(Long id, String taskName, String status, String slot, String ratio) {
        ImageJobDTO dto = new ImageJobDTO();
        dto.setId(id);
        dto.setTaskName(taskName);
        dto.setStatus(status);
        dto.setSlot(slot);
        dto.setRatio(ratio);
        dto.setToolCode("comfyui");
        return dto;
    }

    private GenerationResultDTO buildGenerationResult(Long id, Long imageJobId, String resultUrl) {
        GenerationResultDTO dto = new GenerationResultDTO();
        dto.setId(id);
        dto.setImageJobId(imageJobId);
        dto.setResultUrl(resultUrl);
        dto.setSelected(true);
        return dto;
    }

    private static class RecordingImageJobService implements ImageJobService {
        private ImageJobCreateDTO lastCreate;
        private List<ImageJobDTO> jobsForPlan = List.of();

        @Override public Long createImageJob(ImageJobCreateDTO dto) { lastCreate = dto; return 501L; }
        @Override public PageResult<ImageJobDTO> listImageJobs(int a, int b, String c, String d, String e, Long f) { return PageResult.success(List.of(), 1, 20, 0L); }
        @Override public ImageJobDTO getImageJobById(Long id) { return null; }
        @Override public boolean updateImageJobStatus(Long id, com.ecommerce.detail.ai.dto.ImageJobStatusDTO dto) { return true; }
        @Override public boolean retryImageJob(Long id, com.ecommerce.detail.ai.dto.ImageJobRetryDTO dto) { return true; }
        @Override public boolean cancelImageJob(Long id, com.ecommerce.detail.ai.dto.ImageJobStatusDTO dto) { return true; }
        @Override public List<ImageJobDTO> listByVisualPlanId(Long visualPlanId) { return jobsForPlan; }
    }

    private static class RecordingGenerationResultService implements GenerationResultService {
        private Map<Long, List<GenerationResultDTO>> resultsForJob = Map.of();

        @Override public PageResult<GenerationResultDTO> listGenerationResults(GenerationResultListQuery query) {
            List<GenerationResultDTO> results = resultsForJob.getOrDefault(query.getImageJobId(), List.of());
            return PageResult.success(results, 1, 100, (long) results.size());
        }
        @Override public GenerationResultDTO getGenerationResultById(Long id) { return null; }
        @Override public Long saveGenerationResult(GenerationResultDTO dto) { return null; }
        @Override public Long upsertGenerationResult(GenerationResultDTO dto) { return null; }
        @Override public boolean updateGenerationResultSelection(Long id, GenerationResultSelectionDTO dto) { return false; }
    }

    private CatalogServices catalogServicesWithUnconfirmedPolicy() {
        return new CatalogServices(
            (CategoryVisualPolicyService) Proxy.newProxyInstance(
                CategoryVisualPolicyService.class.getClassLoader(),
                new Class<?>[]{CategoryVisualPolicyService.class},
                (proxy, method, args) -> {
                    if ("getCategoryVisualPolicyById".equals(method.getName())) {
                        CategoryVisualPolicyDTO dto = new CategoryVisualPolicyDTO();
                        dto.setId((Long) args[0]);
                        dto.setCategoryCode("category-" + args[0]);
                        dto.setStatus("DRAFT");
                        return dto;
                    }
                    return defaultValue(method.getReturnType());
                }),
            proxyModelService(),
            proxySkcService(),
            proxyPromptService()
        );
    }

    private VisualPlanServiceImpl service(RecordingVisualPlanMapper mapper, CatalogServices catalogs) {
        return service(mapper, catalogs, null);
    }

    private VisualPlanServiceImpl service(RecordingVisualPlanMapper mapper, CatalogServices catalogs, ImageJobService imageJobSvc) {
        return service(mapper, catalogs, imageJobSvc, null);
    }

    private VisualPlanServiceImpl service(
            RecordingVisualPlanMapper mapper,
            CatalogServices catalogs,
            ImageJobService imageJobSvc,
            GenerationResultService generationResultSvc) {
        VisualPlanServiceImpl service = new VisualPlanServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper.proxy());
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(service, "categoryVisualPolicyService", catalogs.categoryVisualPolicyService);
        ReflectionTestUtils.setField(service, "modelProfileService", catalogs.modelProfileService);
        ReflectionTestUtils.setField(service, "skcPolicyService", catalogs.skcPolicyService);
        ReflectionTestUtils.setField(service, "promptWorkbenchService", catalogs.promptWorkbenchService);
        ReflectionTestUtils.setField(service, "imageJobService", imageJobSvc);
        ReflectionTestUtils.setField(service, "generationResultService", generationResultSvc);
        return service;
    }

    private CatalogServices catalogServices() {
        return new CatalogServices(
                proxyCategoryService(),
                proxyModelService(),
                proxySkcService(),
                proxyPromptService()
        );
    }

    private CategoryVisualPolicyService proxyCategoryService() {
        return (CategoryVisualPolicyService) Proxy.newProxyInstance(
                CategoryVisualPolicyService.class.getClassLoader(),
                new Class<?>[]{CategoryVisualPolicyService.class},
                (proxy, method, args) -> {
                    if ("getCategoryVisualPolicyById".equals(method.getName())) {
                        return categoryPolicy((Long) args[0]);
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private ModelProfileService proxyModelService() {
        return (ModelProfileService) Proxy.newProxyInstance(
                ModelProfileService.class.getClassLoader(),
                new Class<?>[]{ModelProfileService.class},
                (proxy, method, args) -> {
                    if ("getModelProfileById".equals(method.getName())) {
                        return modelProfile((Long) args[0]);
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private SkcPolicyService proxySkcService() {
        return (SkcPolicyService) Proxy.newProxyInstance(
                SkcPolicyService.class.getClassLoader(),
                new Class<?>[]{SkcPolicyService.class},
                (proxy, method, args) -> {
                    if ("getSkcPolicyById".equals(method.getName())) {
                        return skcPolicy((Long) args[0]);
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private PromptWorkbenchService proxyPromptService() {
        return (PromptWorkbenchService) Proxy.newProxyInstance(
                PromptWorkbenchService.class.getClassLoader(),
                new Class<?>[]{PromptWorkbenchService.class},
                (proxy, method, args) -> {
                    if ("getPromptWorkbenchEntryById".equals(method.getName())) {
                        return promptEntry((Long) args[0]);
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private CategoryVisualPolicyDTO categoryPolicy(Long id) {
        CategoryVisualPolicyDTO dto = new CategoryVisualPolicyDTO();
        dto.setId(id);
        dto.setCategoryCode("category-" + id);
        dto.setStatus("CONFIRMED");
        return dto;
    }

    private ModelProfileDTO modelProfile(Long id) {
        ModelProfileDTO dto = new ModelProfileDTO();
        dto.setId(id);
        dto.setDisplayName("model-" + id);
        dto.setStatus("CONFIRMED");
        return dto;
    }

    private SkcPolicyDTO skcPolicy(Long id) {
        SkcPolicyDTO dto = new SkcPolicyDTO();
        dto.setId(id);
        dto.setPolicyName("skc-" + id);
        dto.setStatus("CONFIRMED");
        return dto;
    }

    private PromptWorkbenchEntryDTO promptEntry(Long id) {
        PromptWorkbenchEntryDTO dto = new PromptWorkbenchEntryDTO();
        dto.setId(id);
        dto.setEntryType("GUIDED");
        dto.setStatus("SUCCEEDED");
        dto.setVersion(1);
        dto.setOutputText("prompt-" + id);
        return dto;
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

    private static class RecordingVisualPlanMapper {
        private VisualPlan current;

        private RecordingVisualPlanMapper(VisualPlan stored) {
            this.current = stored;
        }

        private VisualPlanMapper proxy() {
            return (VisualPlanMapper) Proxy.newProxyInstance(
                    VisualPlanMapper.class.getClassLoader(),
                    new Class<?>[]{VisualPlanMapper.class},
                    (proxy, method, args) -> {
                        if ("selectById".equals(method.getName())) {
                            Serializable id = (Serializable) args[0];
                            return current != null && current.getId().equals(id) ? current : null;
                        }
                        if ("insert".equals(method.getName())) {
                            current = (VisualPlan) args[0];
                            current.setId(701L);
                            return 1;
                        }
                        if ("updateById".equals(method.getName())) {
                            current = (VisualPlan) args[0];
                            return 1;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }

    private record CatalogServices(CategoryVisualPolicyService categoryVisualPolicyService,
                                   ModelProfileService modelProfileService,
                                   SkcPolicyService skcPolicyService,
                                   PromptWorkbenchService promptWorkbenchService) {
    }
}

