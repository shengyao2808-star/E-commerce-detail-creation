package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.dto.PublishCheckDTO;
import com.ecommerce.detail.ai.dto.PublishCheckOverrideRequestDTO;
import com.ecommerce.detail.ai.dto.PublishCheckSummaryDTO;
import com.ecommerce.detail.ai.entity.*;
import com.ecommerce.detail.ai.mapper.*;
import com.ecommerce.detail.ai.service.PublishCheckService;
import com.ecommerce.detail.ai.util.RiskCheckUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PublishCheckServiceImpl extends ServiceImpl<PublishCheckMapper, PublishCheck> implements PublishCheckService {

    @Autowired(required = false)
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ProductDetailMapper productDetailMapper;

    @Autowired(required = false)
    private DetailCompositionMapper detailCompositionMapper;

    @Autowired(required = false)
    private DetailCompositionQualityCheckMapper detailCompositionQualityCheckMapper;

    @Autowired(required = false)
    private ExportRecordMapper exportRecordMapper;

    @Autowired(required = false)
    private AuditRecordMapper auditRecordMapper;

    @Autowired(required = false)
    private GenerationResultMapper generationResultMapper;

    @Autowired(required = false)
    private DetailGenerationResultLinkMapper detailGenerationResultLinkMapper;

    @Autowired(required = false)
    private ModelProfileMapper modelProfileMapper;

    @Autowired(required = false)
    private VisualPlanMapper visualPlanMapper;

    @Autowired
    private RiskCheckUtil riskCheckUtil;

    @Override
    public PublishCheckSummaryDTO runChecks(Long productDetailId) {
        if (productDetailId == null) {
            throw new IllegalArgumentException("productDetailId must not be null");
        }
        ProductDetail detail = productDetailMapper.selectById(productDetailId);
        if (detail == null) {
            throw new IllegalArgumentException("Product detail not found: " + productDetailId);
        }

        // Delete previous checks for this detail so the run is idempotent
        LambdaQueryWrapper<PublishCheck> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(PublishCheck::getProductDetailId, productDetailId);
        this.remove(deleteWrapper);

        List<PublishCheck> checks = new ArrayList<>();
        checks.addAll(checkCopyRisk(detail));
        checks.addAll(checkImageQa(detail, productDetailId));
        checks.addAll(checkAssetAuth(detail, productDetailId));
        checks.addAll(checkManifest(productDetailId));
        checks.addAll(checkExportFile(productDetailId));
        checks.addAll(checkAuditStatus(productDetailId));

        for (PublishCheck check : checks) {
            this.save(check);
        }
        return buildSummary(productDetailId, checks);
    }

    @Override
    public List<PublishCheckDTO> listChecks(Long productDetailId) {
        LambdaQueryWrapper<PublishCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PublishCheck::getProductDetailId, productDetailId);
        wrapper.orderByAsc(PublishCheck::getCheckType);
        return this.list(wrapper).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public boolean overrideCheck(Long checkId, PublishCheckOverrideRequestDTO dto) {
        if (checkId == null || dto == null) {
            throw new IllegalArgumentException("checkId and override request must not be null");
        }
        PublishCheck check = this.getById(checkId);
        if (check == null) {
            throw new IllegalArgumentException("Publish check not found: " + checkId);
        }
        if ("PASS".equals(check.getStatus())) {
            throw new IllegalArgumentException("Cannot override a passing check");
        }
        check.setOverridden(1);
        check.setOverrideReason(dto.getReason());
        check.setOverrideOperator(dto.getOperator());
        check.setOverrideTime(LocalDateTime.now());
        check.setStatus("OVERRIDE");
        return this.updateById(check);
    }

    @Override
    public PublishCheckSummaryDTO getSummary(Long productDetailId) {
        List<PublishCheck> checks = this.list(
                new LambdaQueryWrapper<PublishCheck>()
                        .eq(PublishCheck::getProductDetailId, productDetailId));
        return buildSummary(productDetailId, checks);
    }

    // ---- COPY_RISK ----
    private List<PublishCheck> checkCopyRisk(ProductDetail detail) {
        List<PublishCheck> results = new ArrayList<>();
        long id = detail.getId() != null ? detail.getId() : 0L;

        String[] textFields = {"title", "subtitle", "sellingPoints", "description", "aiGeneratedContent"};
        String[] textValues = {
                detail.getTitle(),
                detail.getSubtitle(),
                detail.getSellingPoints(),
                detail.getDescription(),
                detail.getAiGeneratedContent()
        };

        for (int i = 0; i < textFields.length; i++) {
            String text = textValues[i];
            if (!StringUtils.hasText(text)) {
                continue;
            }
            RiskCheckUtil.RiskCheckResult risk = riskCheckUtil.checkRisk(text);
            if (risk.isHasRisk()) {
                PublishCheck check = new PublishCheck();
                check.setProductDetailId(id);
                check.setCheckType("COPY_RISK");
                check.setTargetType("FIELD");
                check.setTargetId(String.valueOf(id));
                check.setTargetField(textFields[i]);
                check.setSeverity("HARD");
                check.setStatus("FAIL");
                check.setMessage("Text risk detected in " + textFields[i] + ": " + String.join("; ", risk.getIssues()));
                check.setDetailsJson(toJson(Map.of(
                        "field", textFields[i],
                        "issues", risk.getIssues(),
                        "issueDetails", risk.getIssueDetails(),
                        "riskLevel", risk.getRiskLevel(),
                        "suggestions", risk.getSuggestions()
                )));
                results.add(check);
            }
        }
        if (results.isEmpty()) {
            PublishCheck pass = new PublishCheck();
            pass.setProductDetailId(id);
            pass.setCheckType("COPY_RISK");
            pass.setTargetType("DETAIL");
            pass.setTargetId(String.valueOf(id));
            pass.setSeverity("HARD");
            pass.setStatus("PASS");
            pass.setMessage("No copy risk detected in detail text fields");
            results.add(pass);
        }
        return results;
    }

    // ---- IMAGE_QA ----
    private List<PublishCheck> checkImageQa(ProductDetail detail, Long detailId) {
        List<PublishCheck> results = new ArrayList<>();

        // Check that detail has at least one generation result applied
        boolean hasImages = false;
        if (detailGenerationResultLinkMapper != null) {
            Long linkCount = detailGenerationResultLinkMapper.selectCount(
                    new LambdaQueryWrapper<DetailGenerationResultLink>()
                            .eq(DetailGenerationResultLink::getProductDetailId, detailId));
            hasImages = linkCount > 0;
        }
        if (!hasImages) {
            // Also check the images JSON field
            String imagesJson = detail.getImages();
            if (StringUtils.hasText(imagesJson)) {
                try {
                    List<String> imageList = objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
                    hasImages = !imageList.isEmpty();
                } catch (Exception ignored) {
                }
            }
        }

        if (!hasImages) {
            PublishCheck check = new PublishCheck();
            check.setProductDetailId(detailId);
            check.setCheckType("IMAGE_QA");
            check.setTargetType("DETAIL");
            check.setTargetId(String.valueOf(detailId));
            check.setTargetField("images");
            check.setSeverity("HARD");
            check.setStatus("FAIL");
            check.setMessage("No images applied to this detail page");
            results.add(check);
        }

        // Check composition quality checks if any compositions exist
        if (detailCompositionMapper != null) {
            List<DetailComposition> compositions = detailCompositionMapper.selectList(
                    new LambdaQueryWrapper<DetailComposition>()
                            .eq(DetailComposition::getProductDetailId, detailId)
                            .eq(DetailComposition::getStatus, "SUCCEEDED"));
            for (DetailComposition comp : compositions) {
                if (detailCompositionQualityCheckMapper != null) {
                    List<DetailCompositionQualityCheck> qcList = detailCompositionQualityCheckMapper.selectList(
                            new LambdaQueryWrapper<DetailCompositionQualityCheck>()
                                    .eq(DetailCompositionQualityCheck::getDetailCompositionId, comp.getId()));
                    if (qcList.isEmpty()) {
                        PublishCheck check = new PublishCheck();
                        check.setProductDetailId(detailId);
                        check.setCheckType("IMAGE_QA");
                        check.setTargetType("COMPOSITION");
                        check.setTargetId(String.valueOf(comp.getId()));
                        check.setTargetField("qualityCheck");
                        check.setSeverity("SOFT");
                        check.setStatus("WARN");
                        check.setMessage("Composition " + comp.getId() + " has no quality check run");
                        results.add(check);
                    } else {
                        boolean anyFailed = qcList.stream().anyMatch(qc -> !"SUCCEEDED".equals(qc.getStatus()));
                        if (anyFailed) {
                            PublishCheck check = new PublishCheck();
                            check.setProductDetailId(detailId);
                            check.setCheckType("IMAGE_QA");
                            check.setTargetType("COMPOSITION");
                            check.setTargetId(String.valueOf(comp.getId()));
                            check.setTargetField("qualityCheck");
                            check.setSeverity("HARD");
                            check.setStatus("FAIL");
                            check.setMessage("Composition " + comp.getId() + " has quality check issues");
                            check.setDetailsJson(toJson(Map.of("compositionId", comp.getId(), "qcStatuses",
                                    qcList.stream().map(DetailCompositionQualityCheck::getStatus).collect(Collectors.toList()))));
                            results.add(check);
                        }
                    }
                }
            }
        }

        if (results.isEmpty()) {
            PublishCheck pass = new PublishCheck();
            pass.setProductDetailId(detailId);
            pass.setCheckType("IMAGE_QA");
            pass.setTargetType("DETAIL");
            pass.setTargetId(String.valueOf(detailId));
            pass.setSeverity("HARD");
            pass.setStatus("PASS");
            pass.setMessage("Image QA passed");
            results.add(pass);
        }
        return results;
    }

    // ---- ASSET_AUTH ----
    private List<PublishCheck> checkAssetAuth(ProductDetail detail, Long detailId) {
        List<PublishCheck> results = new ArrayList<>();

        // Check if any model profiles referenced in visual plans lack authorization
        if (visualPlanMapper != null && modelProfileMapper != null) {
            List<VisualPlan> plans = visualPlanMapper.selectList(
                    new LambdaQueryWrapper<VisualPlan>()
                            .eq(VisualPlan::getProductDetailId, detailId));
            for (VisualPlan plan : plans) {
                if (plan.getModelProfileId() != null) {
                    ModelProfile profile = modelProfileMapper.selectById(plan.getModelProfileId());
                    if (profile == null) {
                        PublishCheck check = new PublishCheck();
                        check.setProductDetailId(detailId);
                        check.setCheckType("ASSET_AUTH");
                        check.setTargetType("ASSET");
                        check.setTargetId(String.valueOf(plan.getModelProfileId()));
                        check.setTargetField("modelProfile");
                        check.setSeverity("HARD");
                        check.setStatus("FAIL");
                        check.setMessage("Model profile " + plan.getModelProfileId() + " referenced in visual plan " + plan.getId() + " not found");
                        results.add(check);
                    } else if (!"AUTHORIZED".equalsIgnoreCase(profile.getAuthorizationStatus())
                            && !"ACTIVE".equalsIgnoreCase(profile.getAuthorizationStatus())
                            && !"CONFIRMED".equalsIgnoreCase(profile.getStatus())) {
                        PublishCheck check = new PublishCheck();
                        check.setProductDetailId(detailId);
                        check.setCheckType("ASSET_AUTH");
                        check.setTargetType("ASSET");
                        check.setTargetId(String.valueOf(profile.getId()));
                        check.setTargetField("authorizationStatus");
                        check.setSeverity("HARD");
                        check.setStatus("FAIL");
                        check.setMessage("Model profile '" + profile.getDisplayName() + "' (ID=" + profile.getId() + ") authorization not confirmed (status=" + profile.getAuthorizationStatus() + ")");
                        check.setDetailsJson(toJson(Map.of("profileId", profile.getId(), "displayName", profile.getDisplayName(),
                                "authorizationStatus", profile.getAuthorizationStatus(), "profileStatus", profile.getStatus())));
                        results.add(check);
                    }
                }
            }
        }

        if (results.isEmpty()) {
            PublishCheck pass = new PublishCheck();
            pass.setProductDetailId(detailId);
            pass.setCheckType("ASSET_AUTH");
            pass.setTargetType("DETAIL");
            pass.setTargetId(String.valueOf(detailId));
            pass.setSeverity("HARD");
            pass.setStatus("PASS");
            pass.setMessage("Asset authorization checks passed");
            results.add(pass);
        }
        return results;
    }

    // ---- MANIFEST ----
    private List<PublishCheck> checkManifest(Long detailId) {
        List<PublishCheck> results = new ArrayList<>();

        if (detailCompositionMapper != null) {
            List<DetailComposition> compositions = detailCompositionMapper.selectList(
                    new LambdaQueryWrapper<DetailComposition>()
                            .eq(DetailComposition::getProductDetailId, detailId));
            boolean hasSucceeded = compositions.stream().anyMatch(c -> "SUCCEEDED".equals(c.getStatus()));
            if (!compositions.isEmpty() && !hasSucceeded) {
                PublishCheck check = new PublishCheck();
                check.setProductDetailId(detailId);
                check.setCheckType("MANIFEST");
                check.setTargetType("COMPOSITION");
                check.setTargetId(String.valueOf(detailId));
                check.setSeverity("HARD");
                check.setStatus("FAIL");
                check.setMessage("Detail has compositions but none succeeded; delivery manifest cannot be generated");
                results.add(check);
            }
        }

        if (results.isEmpty()) {
            PublishCheck pass = new PublishCheck();
            pass.setProductDetailId(detailId);
            pass.setCheckType("MANIFEST");
            pass.setTargetType("DETAIL");
            pass.setTargetId(String.valueOf(detailId));
            pass.setSeverity("SOFT");
            pass.setStatus("PASS");
            pass.setMessage("Manifest check passed");
            results.add(pass);
        }
        return results;
    }

    // ---- EXPORT_FILE ----
    private List<PublishCheck> checkExportFile(Long detailId) {
        List<PublishCheck> results = new ArrayList<>();

        if (exportRecordMapper != null) {
            List<ExportRecord> exports = exportRecordMapper.selectList(
                    new LambdaQueryWrapper<ExportRecord>()
                            .eq(ExportRecord::getProductDetailId, detailId));
            boolean hasSuccess = exports.stream().anyMatch(e -> Integer.valueOf(2).equals(e.getExportStatus()));
            boolean hasPending = exports.stream().anyMatch(e -> Integer.valueOf(0).equals(e.getExportStatus())
                    || Integer.valueOf(1).equals(e.getExportStatus()));
            if (exports.isEmpty()) {
                PublishCheck check = new PublishCheck();
                check.setProductDetailId(detailId);
                check.setCheckType("EXPORT_FILE");
                check.setTargetType("EXPORT");
                check.setTargetId(String.valueOf(detailId));
                check.setSeverity("SOFT");
                check.setStatus("WARN");
                check.setMessage("No export records found for this detail page");
                results.add(check);
            } else if (!hasSuccess) {
                PublishCheck check = new PublishCheck();
                check.setProductDetailId(detailId);
                check.setCheckType("EXPORT_FILE");
                check.setTargetType("EXPORT");
                check.setTargetId(String.valueOf(exports.get(0).getId()));
                check.setSeverity("HARD");
                check.setStatus("FAIL");
                check.setMessage("No successful export found (total records: " + exports.size() + ", pending: " + hasPending + ")");
                results.add(check);
            }
        }

        if (results.isEmpty()) {
            PublishCheck pass = new PublishCheck();
            pass.setProductDetailId(detailId);
            pass.setCheckType("EXPORT_FILE");
            pass.setTargetType("DETAIL");
            pass.setTargetId(String.valueOf(detailId));
            pass.setSeverity("SOFT");
            pass.setStatus("PASS");
            pass.setMessage("Export file check passed");
            results.add(pass);
        }
        return results;
    }

    // ---- AUDIT_STATUS ----
    private List<PublishCheck> checkAuditStatus(Long detailId) {
        List<PublishCheck> results = new ArrayList<>();

        if (auditRecordMapper != null) {
            AuditRecord record = auditRecordMapper.selectOne(
                    new LambdaQueryWrapper<AuditRecord>()
                            .eq(AuditRecord::getProductDetailId, detailId)
                            .orderByDesc(AuditRecord::getAuditTime)
                            .last("LIMIT 1"));
            if (record == null) {
                PublishCheck check = new PublishCheck();
                check.setProductDetailId(detailId);
                check.setCheckType("AUDIT_STATUS");
                check.setTargetType("DETAIL");
                check.setTargetId(String.valueOf(detailId));
                check.setSeverity("HARD");
                check.setStatus("FAIL");
                check.setMessage("No audit record found; detail must be submitted and approved before publishing");
                results.add(check);
            } else if (!Integer.valueOf(2).equals(record.getAuditStatus())) {
                PublishCheck check = new PublishCheck();
                check.setProductDetailId(detailId);
                check.setCheckType("AUDIT_STATUS");
                check.setTargetType("TASK");
                check.setTargetId(String.valueOf(record.getId()));
                check.setTargetField("auditStatus");
                check.setSeverity("HARD");
                check.setStatus("FAIL");
                check.setMessage("Audit not approved (current status=" + record.getAuditStatus() + ")");
                check.setDetailsJson(toJson(Map.of("auditRecordId", record.getId(), "auditStatus", record.getAuditStatus(),
                        "auditor", record.getAuditor(), "auditComment", record.getAuditComment())));
                results.add(check);
            }
        }

        if (results.isEmpty()) {
            PublishCheck pass = new PublishCheck();
            pass.setProductDetailId(detailId);
            pass.setCheckType("AUDIT_STATUS");
            pass.setTargetType("DETAIL");
            pass.setTargetId(String.valueOf(detailId));
            pass.setSeverity("HARD");
            pass.setStatus("PASS");
            pass.setMessage("Audit approved");
            results.add(pass);
        }
        return results;
    }

    // ---- helpers ----

    private PublishCheckSummaryDTO buildSummary(Long productDetailId, List<PublishCheck> checks) {
        PublishCheckSummaryDTO summary = new PublishCheckSummaryDTO();
        summary.setProductDetailId(productDetailId);
        summary.setTotalChecks(checks.size());
        summary.setPassedChecks((int) checks.stream().filter(c -> "PASS".equals(c.getStatus())).count());
        summary.setFailedChecks((int) checks.stream().filter(c -> "FAIL".equals(c.getStatus())).count());
        summary.setOverriddenChecks((int) checks.stream().filter(c -> "OVERRIDE".equals(c.getStatus())).count());
        summary.setHardFailedChecks((int) checks.stream().filter(c -> "FAIL".equals(c.getStatus()) && "HARD".equals(c.getSeverity())).count());
        summary.setSoftFailedChecks((int) checks.stream().filter(c -> "FAIL".equals(c.getStatus()) && "SOFT".equals(c.getSeverity())).count());
        summary.setItems(checks.stream().map(this::toDTO).collect(Collectors.toList()));
        // Publishable: no HARD failures (all HARD checks are PASS or OVERRIDE)
        boolean hasHardFail = checks.stream().anyMatch(c -> "FAIL".equals(c.getStatus()) && "HARD".equals(c.getSeverity()));
        summary.setPublishable(!hasHardFail);
        return summary;
    }

    private PublishCheckDTO toDTO(PublishCheck entity) {
        PublishCheckDTO dto = new PublishCheckDTO();
        dto.setId(entity.getId());
        dto.setProductDetailId(entity.getProductDetailId());
        dto.setCheckType(entity.getCheckType());
        dto.setTargetType(entity.getTargetType());
        dto.setTargetId(entity.getTargetId());
        dto.setTargetField(entity.getTargetField());
        dto.setSeverity(entity.getSeverity());
        dto.setStatus(entity.getStatus());
        dto.setMessage(entity.getMessage());
        dto.setOverridden(Integer.valueOf(1).equals(entity.getOverridden()));
        dto.setOverrideReason(entity.getOverrideReason());
        dto.setOverrideOperator(entity.getOverrideOperator());
        dto.setOverrideTime(entity.getOverrideTime());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        if (StringUtils.hasText(entity.getDetailsJson())) {
            try {
                dto.setDetails(objectMapper.readValue(entity.getDetailsJson(), new TypeReference<Map<String, Object>>() {}));
            } catch (Exception ignored) {
                dto.setDetails(entity.getDetailsJson());
            }
        }
        return dto;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
