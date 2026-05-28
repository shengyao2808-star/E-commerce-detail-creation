package com.ecommerce.detail.ai.service.impl;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.CategoryVisualPolicyDTO;
import com.ecommerce.detail.ai.dto.ModelProfileDTO;
import com.ecommerce.detail.ai.dto.PromptWorkbenchEntryDTO;
import com.ecommerce.detail.ai.dto.SkcPolicyDTO;
import com.ecommerce.detail.ai.dto.VisualPlanConfirmDTO;
import com.ecommerce.detail.ai.dto.VisualPlanDTO;
import com.ecommerce.detail.ai.entity.VisualPlan;
import com.ecommerce.detail.ai.mapper.VisualPlanMapper;
import com.ecommerce.detail.ai.service.CategoryVisualPolicyService;
import com.ecommerce.detail.ai.service.ModelProfileService;
import com.ecommerce.detail.ai.service.PromptWorkbenchService;
import com.ecommerce.detail.ai.service.SkcPolicyService;
import com.ecommerce.detail.ai.dto.ImageJobCreateDTO;
import com.ecommerce.detail.ai.dto.ImageJobDTO;
import com.ecommerce.detail.ai.dto.GenerationResultDTO;
import com.ecommerce.detail.ai.dto.GenerationResultListQuery;
import com.ecommerce.detail.ai.dto.VisualPlanBatchStatusDTO;


import com.ecommerce.detail.ai.service.GenerationResultService;
import com.ecommerce.detail.ai.service.ImageJobService;
import com.ecommerce.detail.ai.service.VisualPlanService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class VisualPlanServiceImpl extends ServiceImpl<VisualPlanMapper, VisualPlan> implements VisualPlanService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("DRAFT", "CONFIRMED");
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<Long>> LONG_LIST_TYPE = new TypeReference<>() {};

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryVisualPolicyService categoryVisualPolicyService;

    @Autowired
    private ModelProfileService modelProfileService;

    @Autowired
    private SkcPolicyService skcPolicyService;

    @Autowired
    private PromptWorkbenchService promptWorkbenchService;

    @Autowired
    private ImageJobService imageJobService;

    @Autowired(required = false)
    private GenerationResultService generationResultService;

    @Override
    public PageResult<VisualPlanDTO> listVisualPlans(int pageNum, int pageSize, Long productDetailId, String status) {
        pageNum = Math.max(pageNum, 1);
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        LambdaQueryWrapper<VisualPlan> wrapper = new LambdaQueryWrapper<>();
        if (productDetailId != null) {
            wrapper.eq(VisualPlan::getProductDetailId, productDetailId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(VisualPlan::getStatus, normalizeStatus(status));
        }
        wrapper.orderByDesc(VisualPlan::getUpdateTime);

        Page<VisualPlan> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<VisualPlanDTO> records = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.success(records, pageNum, pageSize, page.getTotal());
    }

    @Override
    public Long createVisualPlan(VisualPlanDTO dto) {
        VisualPlan plan = new VisualPlan();
        plan.setProductDetailId(dto == null ? null : dto.getProductDetailId());
        plan.setPlanName(requireText(dto == null ? null : dto.getPlanName(), "planName"));
        plan.setCategoryCode(dto == null ? null : dto.getCategoryCode());
        plan.setCategoryVisualPolicyId(dto == null ? null : dto.getCategoryVisualPolicyId());
        plan.setModelProfileId(dto == null ? null : dto.getModelProfileId());
        plan.setSkcPolicyId(dto == null ? null : dto.getSkcPolicyId());
        plan.setPromptWorkbenchEntryIdsJson(writeJson(dto == null || dto.getPromptWorkbenchEntryIds() == null
                ? List.of()
                : dto.getPromptWorkbenchEntryIds()));
        plan.setInputJson(writeJson(dto == null ? null : dto.getInputData()));
        plan.setPlanJson(writeJson(dto == null ? null : dto.getPlanData()));
        plan.setConfirmedSnapshotJson(null);
        plan.setStatus("DRAFT");
        plan.setVersion(1);
        plan.setConfirmedTime(null);
        plan.setCreateTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
        this.save(plan);
        return plan.getId();
    }

    @Override
    public VisualPlanDTO getVisualPlanById(Long id) {
        return toDTO(requirePlan(id));
    }

    @Override
    public boolean updateVisualPlan(Long id, VisualPlanDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("plan data must not be null");
        }
        VisualPlan plan = requirePlan(id);
        requireDraft(plan);
        if (dto.getPlanName() != null) {
            plan.setPlanName(requireText(dto.getPlanName(), "planName"));
        }
        if (dto.getProductDetailId() != null) {
            plan.setProductDetailId(dto.getProductDetailId());
        }
        if (dto.getCategoryCode() != null) {
            plan.setCategoryCode(dto.getCategoryCode());
        }
        if (dto.getCategoryVisualPolicyId() != null) {
            plan.setCategoryVisualPolicyId(dto.getCategoryVisualPolicyId());
        }
        if (dto.getModelProfileId() != null) {
            plan.setModelProfileId(dto.getModelProfileId());
        }
        if (dto.getSkcPolicyId() != null) {
            plan.setSkcPolicyId(dto.getSkcPolicyId());
        }
        if (dto.getPromptWorkbenchEntryIds() != null) {
            plan.setPromptWorkbenchEntryIdsJson(writeJson(dto.getPromptWorkbenchEntryIds()));
        }
        if (dto.getInputData() != null) {
            plan.setInputJson(writeJson(dto.getInputData()));
        }
        if (dto.getPlanData() != null) {
            plan.setPlanJson(writeJson(dto.getPlanData()));
        }
        plan.setVersion(plan.getVersion() == null ? 1 : plan.getVersion() + 1);
        plan.setUpdateTime(LocalDateTime.now());
        return this.updateById(plan);
    }

    @Override
    public List<Long> dispatchFromVisualPlan(Long id, List<ImageJobCreateDTO> jobs) {
        VisualPlan plan = requirePlan(id);
        if (!"CONFIRMED".equals(plan.getStatus())) {
            throw new IllegalStateException("only CONFIRMED visual plans can dispatch image jobs: " + id);
        }
        if (jobs == null || jobs.isEmpty()) {
            throw new IllegalArgumentException("jobs list must not be empty");
        }
        List<Long> jobIds = new ArrayList<>();
        for (ImageJobCreateDTO job : jobs) {
            if (job == null) {
                throw new IllegalArgumentException("job entry must not be null");
            }
            job.setVisualPlanId(id);
            job.setModelProfileId(plan.getModelProfileId());
            job.setSourceSnapshotJson(plan.getConfirmedSnapshotJson());
            jobIds.add(imageJobService.createImageJob(job));
        }
        return jobIds;
    }

    @Override
    public VisualPlanBatchStatusDTO getBatchStatus(Long id) {
        requirePlan(id);
        List<ImageJobDTO> jobDTOs = imageJobService.listByVisualPlanId(id);
        VisualPlanBatchStatusDTO status = new VisualPlanBatchStatusDTO();
        status.setVisualPlanId(id);
        status.setTotalJobs(jobDTOs.size());
        int succeeded = 0, failed = 0, pending = 0, running = 0, canceled = 0;
        List<Map<String, Object>> summaries = new ArrayList<>();
        for (ImageJobDTO job : jobDTOs) {
            String s = job.getStatus() == null ? "PENDING" : job.getStatus().toUpperCase();
            switch (s) {
                case "SUCCEEDED" -> succeeded++;
                case "FAILED" -> failed++;
                case "RUNNING" -> running++;
                case "PENDING" -> pending++;
                case "CANCELED" -> canceled++;
                default -> pending++;
            }
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("id", job.getId());
            summary.put("taskName", job.getTaskName());
            summary.put("status", job.getStatus());
            summary.put("slot", job.getSlot());
            summary.put("ratio", job.getRatio());
            summary.put("progress", job.getProgress());
            summary.put("promptVersion", job.getPromptVersion());
            summary.put("modelProfileId", job.getModelProfileId());
            summary.put("errorMessage", job.getErrorMessage());
            summaries.add(summary);
        }
        status.setSucceededJobs(succeeded);
        status.setFailedJobs(failed);
        status.setPendingJobs(pending);
        status.setRunningJobs(running);
        status.setCanceledJobs(canceled);
        status.setJobSummaries(summaries);
        if (jobDTOs.isEmpty()) {
            status.setAggregatedStatus("CANCELED");
        } else if (failed > 0 && succeeded == 0) {
            status.setAggregatedStatus("FAILED");
        } else if (pending > 0 || running > 0) {
            status.setAggregatedStatus("RUNNING");
        } else if (succeeded == jobDTOs.size()) {
            status.setAggregatedStatus("SUCCEEDED");
        } else {
            status.setAggregatedStatus("PARTIAL_SUCCEEDED");
        }
        return status;
    }

    @Override
    public VisualPlanDTO confirmVisualPlan(Long id, VisualPlanConfirmDTO dto) {
        VisualPlan plan = requirePlan(id);
        if ("CONFIRMED".equals(plan.getStatus())) {
            return toDTO(plan);
        }

        // Validate that referenced catalog entities are CONFIRMED
        if (plan.getCategoryVisualPolicyId() != null) {
            CategoryVisualPolicyDTO policy = categoryVisualPolicyService.getCategoryVisualPolicyById(plan.getCategoryVisualPolicyId());
            requireConfirmedEntity("categoryVisualPolicy", policy == null ? null : policy.getStatus(), plan.getCategoryVisualPolicyId());
        }
        if (plan.getModelProfileId() != null) {
            ModelProfileDTO model = modelProfileService.getModelProfileById(plan.getModelProfileId());
            requireConfirmedEntity("modelProfile", model == null ? null : model.getStatus(), plan.getModelProfileId());
        }
        if (plan.getSkcPolicyId() != null) {
            SkcPolicyDTO skc = skcPolicyService.getSkcPolicyById(plan.getSkcPolicyId());
            requireConfirmedEntity("skcPolicy", skc == null ? null : skc.getStatus(), plan.getSkcPolicyId());
        }

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("visualPlan", snapshotPlan(toDTO(plan)));
        snapshot.put("confirmData", dto == null ? Map.of() : dto.getConfirmData());
        snapshot.put("categoryVisualPolicy", snapshotCategoryPolicy(resolveCategoryVisualPolicy(plan.getCategoryVisualPolicyId())));
        snapshot.put("modelProfile", snapshotModelProfile(resolveModelProfile(plan.getModelProfileId())));
        snapshot.put("skcPolicy", snapshotSkcPolicy(resolveSkcPolicy(plan.getSkcPolicyId())));
        snapshot.put("promptWorkbenchEntries", snapshotPromptEntries(resolvePromptWorkbenchEntries(plan.getPromptWorkbenchEntryIdsJson())));

        plan.setConfirmedSnapshotJson(writeJson(snapshot));
        plan.setStatus("CONFIRMED");
        plan.setVersion(plan.getVersion() == null ? 1 : plan.getVersion() + 1);
        plan.setConfirmedTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
        this.updateById(plan);
        return toDTO(plan);
    }

    private CategoryVisualPolicyDTO resolveCategoryVisualPolicy(Long id) {
        return id == null ? null : categoryVisualPolicyService.getCategoryVisualPolicyById(id);
    }

    private Map<String, Object> snapshotCategoryPolicy(CategoryVisualPolicyDTO dto) {
        if (dto == null) {
            return Map.of();
        }
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", dto.getId());
        snapshot.put("categoryCode", dto.getCategoryCode());
        snapshot.put("categoryName", dto.getCategoryName());
        snapshot.put("modelPolicy", dto.getModelPolicy());
        snapshot.put("modelConsistencyLevel", dto.getModelConsistencyLevel());
        snapshot.put("allowedShotTypes", dto.getAllowedShotTypes());
        snapshot.put("requiredMainImages", dto.getRequiredMainImages());
        snapshot.put("detailScreenCountRange", dto.getDetailScreenCountRange());
        snapshot.put("riskRules", dto.getRiskRules());
        snapshot.put("status", dto.getStatus());
        snapshot.put("version", dto.getVersion());
        return snapshot;
    }

    private ModelProfileDTO resolveModelProfile(Long id) {
        return id == null ? null : modelProfileService.getModelProfileById(id);
    }

    private Map<String, Object> snapshotModelProfile(ModelProfileDTO dto) {
        if (dto == null) {
            return Map.of();
        }
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", dto.getId());
        snapshot.put("displayName", dto.getDisplayName());
        snapshot.put("frontImage", dto.getFrontImage());
        snapshot.put("sideImage", dto.getSideImage());
        snapshot.put("backImage", dto.getBackImage());
        snapshot.put("height", dto.getHeight());
        snapshot.put("weight", dto.getWeight());
        snapshot.put("bust", dto.getBust());
        snapshot.put("waist", dto.getWaist());
        snapshot.put("hip", dto.getHip());
        snapshot.put("styleTags", dto.getStyleTags());
        snapshot.put("categoryScopes", dto.getCategoryScopes());
        snapshot.put("authorizationStatus", dto.getAuthorizationStatus());
        snapshot.put("status", dto.getStatus());
        snapshot.put("version", dto.getVersion());
        return snapshot;
    }

    private SkcPolicyDTO resolveSkcPolicy(Long id) {
        return id == null ? null : skcPolicyService.getSkcPolicyById(id);
    }

    private Map<String, Object> snapshotSkcPolicy(SkcPolicyDTO dto) {
        if (dto == null) {
            return Map.of();
        }
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", dto.getId());
        snapshot.put("policyName", dto.getPolicyName());
        snapshot.put("categoryCode", dto.getCategoryCode());
        snapshot.put("colorCount", dto.getColorCount());
        snapshot.put("specCount", dto.getSpecCount());
        snapshot.put("colors", dto.getColors());
        snapshot.put("specs", dto.getSpecs());
        snapshot.put("renderMode", dto.getRenderMode());
        snapshot.put("variantDisplayMode", dto.getVariantDisplayMode());
        snapshot.put("generationRules", dto.getGenerationRules());
        snapshot.put("status", dto.getStatus());
        snapshot.put("version", dto.getVersion());
        return snapshot;
    }

    private List<PromptWorkbenchEntryDTO> resolvePromptWorkbenchEntries(String json) {
        List<Long> ids = readLongList(json);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<PromptWorkbenchEntryDTO> entries = new ArrayList<>();
        for (Long entryId : ids) {
            entries.add(promptWorkbenchService.getPromptWorkbenchEntryById(entryId));
        }
        return entries;
    }

    private List<Map<String, Object>> snapshotPromptEntries(List<PromptWorkbenchEntryDTO> entries) {
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> snapshots = new ArrayList<>();
        for (PromptWorkbenchEntryDTO dto : entries) {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("id", dto.getId());
            snapshot.put("entryType", dto.getEntryType());
            snapshot.put("taskName", dto.getTaskName());
            snapshot.put("toolCode", dto.getToolCode());
            snapshot.put("status", dto.getStatus());
            snapshot.put("version", dto.getVersion());
            snapshot.put("inputData", dto.getInputData());
            snapshot.put("outputData", dto.getOutputData());
            snapshot.put("outputText", dto.getOutputText());
            snapshot.put("errorMessage", dto.getErrorMessage());
            snapshots.add(snapshot);
        }
        return snapshots;
    }

    private Map<String, Object> snapshotPlan(VisualPlanDTO dto) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", dto.getId());
        snapshot.put("productDetailId", dto.getProductDetailId());
        snapshot.put("planName", dto.getPlanName());
        snapshot.put("categoryCode", dto.getCategoryCode());
        snapshot.put("categoryVisualPolicyId", dto.getCategoryVisualPolicyId());
        snapshot.put("modelProfileId", dto.getModelProfileId());
        snapshot.put("skcPolicyId", dto.getSkcPolicyId());
        snapshot.put("promptWorkbenchEntryIds", dto.getPromptWorkbenchEntryIds());
        snapshot.put("inputData", dto.getInputData());
        snapshot.put("planData", dto.getPlanData());
        snapshot.put("status", dto.getStatus());
        snapshot.put("version", dto.getVersion());
        return snapshot;
    }

    private void requireDraft(VisualPlan plan) {
        if (plan == null || !"DRAFT".equals(plan.getStatus())) {
            throw new IllegalStateException("visual plan must be in DRAFT status: " + (plan == null ? null : plan.getId()));
        }
    }

    private void requireConfirmedEntity(String entityType, String status, Long id) {
        if (id == null) {
            return;
        }
        if (!"CONFIRMED".equals(status)) {
            throw new IllegalStateException(entityType + " must be CONFIRMED before plan confirmation: " + id);
        }
    }

    private VisualPlan requirePlan(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        VisualPlan plan = this.getById(id);
        if (plan == null) {
            throw new IllegalStateException("visual plan not found: " + id);
        }
        return plan;
    }

    private VisualPlanDTO toDTO(VisualPlan plan) {
        VisualPlanDTO dto = new VisualPlanDTO();
        dto.setId(plan.getId());
        dto.setProductDetailId(plan.getProductDetailId());
        dto.setPlanName(plan.getPlanName());
        dto.setCategoryCode(plan.getCategoryCode());
        dto.setCategoryVisualPolicyId(plan.getCategoryVisualPolicyId());
        dto.setModelProfileId(plan.getModelProfileId());
        dto.setSkcPolicyId(plan.getSkcPolicyId());
        dto.setPromptWorkbenchEntryIds(readLongList(plan.getPromptWorkbenchEntryIdsJson()));
        dto.setInputData(readMap(plan.getInputJson()));
        dto.setPlanData(readMap(plan.getPlanJson()));
        dto.setSnapshotData(readMap(plan.getConfirmedSnapshotJson()));
        dto.setStatus(plan.getStatus());
        dto.setVersion(plan.getVersion());
        dto.setConfirmedTime(plan.getConfirmedTime());
        dto.setCreateTime(plan.getCreateTime());
        dto.setUpdateTime(plan.getUpdateTime());
        return dto;
    }

    private String normalizeStatus(String status) {
        String normalized = requireText(status, "status").toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("unsupported visual plan status: " + status);
        }
        return normalized;
    }

    private String requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize visual plan json", e);
        }
    }

    private Map<String, Object> readMap(String json) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            return Map.of();
        }
    }

    private List<Long> readLongList(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, LONG_LIST_TYPE);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public int retryAllFailedJobs(Long planId) {
        requirePlan(planId);
        List<ImageJobDTO> jobs = imageJobService.listByVisualPlanId(planId);
        int retried = 0;
        for (ImageJobDTO job : jobs) {
            String s = job.getStatus() == null ? "" : job.getStatus().toUpperCase();
            if ("FAILED".equals(s) || "CANCELED".equals(s)) {
                com.ecommerce.detail.ai.dto.ImageJobRetryDTO retryDto = new com.ecommerce.detail.ai.dto.ImageJobRetryDTO();
                retryDto.setRetryReason("Batch retry triggered");
                try {
                    imageJobService.retryImageJob(job.getId(), retryDto);
                    retried++;
                } catch (IllegalStateException ignored) {
                    // terminal state race — skip
                }
            }
        }
        return retried;
    }

    @Override
    public int cancelBatch(Long planId) {
        requirePlan(planId);
        List<ImageJobDTO> jobs = imageJobService.listByVisualPlanId(planId);
        int canceled = 0;
        for (ImageJobDTO job : jobs) {
            String s = job.getStatus() == null ? "" : job.getStatus().toUpperCase();
            if ("PENDING".equals(s) || "RUNNING".equals(s)) {
                com.ecommerce.detail.ai.dto.ImageJobStatusDTO cancelDto = new com.ecommerce.detail.ai.dto.ImageJobStatusDTO();
                cancelDto.setStatus("CANCELED");
                cancelDto.setErrorMessage("Batch cancel triggered");
                try {
                    imageJobService.cancelImageJob(job.getId(), cancelDto);
                    canceled++;
                } catch (IllegalStateException ignored) {
                    // terminal state race — skip
                }
            }
        }
        return canceled;
    }

    @Override
    public Map<String, Object> getBatchResultsBySlot(Long planId, String slotFilter) {
        requirePlan(planId);
        List<ImageJobDTO> jobs = imageJobService.listByVisualPlanId(planId);

        Map<String, List<Map<String, Object>>> slotGroups = new LinkedHashMap<>();
        for (ImageJobDTO job : jobs) {
            String slot = StringUtils.hasText(job.getSlot()) ? job.getSlot() : "default";
            if (StringUtils.hasText(slotFilter) && !slotFilter.equals(slot)) {
                continue;
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("jobId", job.getId());
            entry.put("taskName", job.getTaskName());
            entry.put("status", job.getStatus());
            entry.put("slot", job.getSlot());
            entry.put("ratio", job.getRatio());
            entry.put("progress", job.getProgress());
            entry.put("errorMessage", job.getErrorMessage());
            entry.put("results", listResultsForJob(job.getId()));
            slotGroups.computeIfAbsent(slot, k -> new ArrayList<>()).add(entry);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("visualPlanId", planId);
        result.put("slotFilter", slotFilter);
        result.put("totalSlots", slotGroups.size());
        result.put("slotGroups", slotGroups);
        return result;
    }

    private List<GenerationResultDTO> listResultsForJob(Long imageJobId) {
        if (generationResultService == null || imageJobId == null) {
            return List.of();
        }
        GenerationResultListQuery query = new GenerationResultListQuery();
        query.setPageNum(1);
        query.setPageSize(100);
        query.setImageJobId(imageJobId);
        return generationResultService.listGenerationResults(query).getData();
    }
}
