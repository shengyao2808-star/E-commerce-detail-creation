package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.enums.TaskStatus;
import com.ecommerce.detail.ai.dto.DetailCompositionCreateDTO;
import com.ecommerce.detail.ai.dto.DetailCompositionDTO;
import com.ecommerce.detail.ai.dto.DetailCompositionListQuery;
import com.ecommerce.detail.ai.dto.DetailCompositionQualityCheckDTO;
import com.ecommerce.detail.ai.dto.DetailDeliveryManifestDTO;
import com.ecommerce.detail.ai.dto.ProductDetailDTO;
import com.ecommerce.detail.ai.dto.tool.ToolAdapterInfoDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeRequestDTO;
import com.ecommerce.detail.ai.dto.tool.ToolInvokeResponseDTO;
import com.ecommerce.detail.ai.entity.DetailComposition;
import com.ecommerce.detail.ai.entity.DetailCompositionQualityCheck;
import com.ecommerce.detail.ai.entity.DetailCompositionResult;
import com.ecommerce.detail.ai.entity.DetailGenerationResultLink;
import com.ecommerce.detail.ai.entity.GenerationResult;
import com.ecommerce.detail.ai.entity.ProductDetail;
import com.ecommerce.detail.ai.mapper.DetailCompositionMapper;
import com.ecommerce.detail.ai.mapper.DetailCompositionQualityCheckMapper;
import com.ecommerce.detail.ai.mapper.DetailCompositionResultMapper;
import com.ecommerce.detail.ai.mapper.DetailGenerationResultLinkMapper;
import com.ecommerce.detail.ai.mapper.GenerationResultMapper;
import com.ecommerce.detail.ai.mapper.ProductDetailMapper;
import com.ecommerce.detail.ai.service.DetailCompositionService;
import com.ecommerce.detail.ai.service.ToolAdapterService;
import com.ecommerce.detail.ai.util.FileUtil;
import com.ecommerce.detail.ai.util.LocalPathPolicy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class DetailCompositionServiceImpl extends ServiceImpl<DetailCompositionMapper, DetailComposition> implements DetailCompositionService {

    private static final String DEFAULT_TOOL_CODE = "imagemagick";
    private static final String DEFAULT_QA_TOOL_CODE = "playwright";
    private static final String DEFAULT_QA_OPERATION = "verify-page";
    private static final String DEFAULT_OUTPUT_DIR = "exports/detail-compositions";
    private static final String DEFAULT_QA_DIR = "exports/detail-compositions/quality-checks";
    private static final String DEFAULT_MIME_TYPE = "image/png";
    private static final String DEFAULT_OUTPUT_RATIO = "750xauto";
    private static final List<String> DEFAULT_OUTPUT_ROOTS = List.of("exports/detail-compositions");

    @Autowired(required = false)
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired(required = false)
    private Environment environment;

    @Autowired(required = false)
    private ToolAdapterService toolAdapterService;

    @Autowired(required = false)
    private TaskExecutor taskExecutor;

    @Autowired
    private ProductDetailMapper productDetailMapper;

    @Autowired
    private DetailCompositionResultMapper detailCompositionResultMapper;

    @Autowired
    private DetailCompositionQualityCheckMapper detailCompositionQualityCheckMapper;

    @Autowired
    private DetailGenerationResultLinkMapper detailGenerationResultLinkMapper;

    @Autowired(required = false)
    private GenerationResultMapper generationResultMapper;

    @Override
    public Long createDetailComposition(DetailCompositionCreateDTO dto) {
        if (dto == null || dto.getProductDetailId() == null) {
            throw new IllegalArgumentException("productDetailId must not be null");
        }

        ProductDetail detail = requireDetail(dto.getProductDetailId());
        DetailComposition job = new DetailComposition();
        job.setProductDetailId(dto.getProductDetailId());
        job.setTaskName(resolveTaskName(dto, detail));
        job.setToolCode(resolveToolCode(dto));
        job.setInputJson(writeJson(buildInputSnapshot(dto, detail)));
        job.setStatus(TaskStatus.PENDING.getCode());
        job.setProgress(0);
        job.setCreateTime(now());
        job.setUpdateTime(now());
        this.save(job);

        job.setOutputPath(buildOutputPath(job.getId(), detail));
        ensureOutputDirectory(job.getOutputPath());
        job.setUpdateTime(now());
        this.updateById(job);

        submitAsync(job.getId());
        return job.getId();
    }

    @Override
    public DetailCompositionDTO getDetailCompositionById(Long id) {
        DetailComposition job = requireJob(id);
        return toDTO(job);
    }

    @Override
    public PageResult<DetailCompositionDTO> listDetailCompositions(DetailCompositionListQuery query) {
        DetailCompositionListQuery safeQuery = query == null ? new DetailCompositionListQuery() : query;
        int pageNum = safeQuery.getPageNum() == null ? 1 : Math.max(safeQuery.getPageNum(), 1);
        int pageSize = safeQuery.getPageSize() == null ? 20 : safeQuery.getPageSize();
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        LambdaQueryWrapper<DetailComposition> wrapper = new LambdaQueryWrapper<>();
        if (safeQuery.getProductDetailId() != null) {
            wrapper.eq(DetailComposition::getProductDetailId, safeQuery.getProductDetailId());
        }
        if (StringUtils.hasText(safeQuery.getStatus())) {
            wrapper.eq(DetailComposition::getStatus, normalizeStatus(safeQuery.getStatus()));
        }
        if (StringUtils.hasText(safeQuery.getToolCode())) {
            wrapper.eq(DetailComposition::getToolCode, safeQuery.getToolCode().trim());
        }
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            wrapper.and(q -> q.like(DetailComposition::getTaskName, safeQuery.getKeyword())
                    .or()
                    .like(DetailComposition::getExternalJobId, safeQuery.getKeyword())
                    .or()
                    .like(DetailComposition::getOutputPath, safeQuery.getKeyword()));
        }
        wrapper.orderByDesc(DetailComposition::getCreateTime);

        Page<DetailComposition> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<DetailCompositionDTO> records = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.success(records, pageNum, pageSize, page.getTotal());
    }

    @Override
    public File resolveDownloadFile(Long id) {
        DetailComposition job = requireJob(id);
        if (!TaskStatus.SUCCEEDED.getCode().equals(TaskStatus.normalize(job.getStatus(), ""))) {
            throw new IllegalStateException("Detail composition has not completed successfully");
        }
        if (!StringUtils.hasText(job.getOutputPath())) {
            throw new IllegalStateException("Detail composition output path is empty");
        }

        Path outputPath = LocalPathPolicy.requirePathWithinRoots(
                job.getOutputPath(),
                allowedOutputRoots(),
                "detail composition output");
        if (!Files.isRegularFile(outputPath) || !Files.isReadable(outputPath)) {
            throw new IllegalStateException("Detail composition file does not exist: " + job.getOutputPath());
        }
        try {
            if (Files.size(outputPath) <= 0) {
                throw new IllegalStateException("Detail composition file is empty: " + job.getOutputPath());
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to verify detail composition output file: " + job.getOutputPath(), e);
        }
        return outputPath.toFile();
    }

    @Override
    public Long createQualityCheck(Long id) {
        if (detailCompositionQualityCheckMapper == null) {
            throw new IllegalStateException("Detail composition quality-check persistence is not available");
        }

        DetailComposition job = requireJob(id);
        DetailCompositionQualityCheck check = new DetailCompositionQualityCheck();
        check.setDetailCompositionId(job.getId());
        check.setToolCode(DEFAULT_QA_TOOL_CODE);
        check.setStatus(TaskStatus.PENDING.getCode());
        check.setIssueCount(0);
        check.setIssuesJson(writeJson(List.of()));
        check.setCreateTime(now());
        check.setUpdateTime(now());
        detailCompositionQualityCheckMapper.insert(check);

        submitQualityCheckAsync(check.getId());
        return check.getId();
    }

    @Override
    public PageResult<DetailCompositionQualityCheckDTO> listQualityChecks(Long id, Integer pageNum, Integer pageSize) {
        requireJob(id);
        int safePageNum = pageNum == null ? 1 : Math.max(pageNum, 1);
        int safePageSize = pageSize == null ? 20 : pageSize;
        if (safePageSize < 1 || safePageSize > 100) {
            safePageSize = 20;
        }

        List<DetailCompositionQualityCheck> filtered = loadQualityChecks(id);
        int total = filtered.size();
        int fromIndex = Math.min((safePageNum - 1) * safePageSize, total);
        int toIndex = Math.min(fromIndex + safePageSize, total);
        List<DetailCompositionQualityCheckDTO> records = filtered.subList(fromIndex, toIndex).stream()
                .map(this::toQualityCheckDTO)
                .toList();
        return PageResult.success(records, safePageNum, safePageSize, (long) total);
    }

    @Override
    public DetailDeliveryManifestDTO getDeliveryManifest(Long id) {
        DetailCompositionDTO composition = getDetailCompositionById(id);
        DetailDeliveryManifestDTO manifest = new DetailDeliveryManifestDTO();
        manifest.setDetailCompositionId(composition.getId());
        manifest.setProductDetailId(composition.getProductDetailId());
        manifest.setDeliverable(Boolean.TRUE.equals(composition.getDeliverable()));
        manifest.setCompositionStatus(composition.getStatus());
        manifest.setOutputPath(composition.getOutputPath());
        manifest.setOutputFileName(composition.getOutputFileName());
        manifest.setOutputFileSize(composition.getOutputFileSize());
        manifest.setOutputWidth(composition.getOutputWidth());
        manifest.setOutputHeight(composition.getOutputHeight());
        manifest.setLatestQualityCheckStatus(composition.getLatestQualityCheckStatus());
        manifest.setLatestQualityCheckIssueCount(composition.getLatestQualityCheckIssueCount());
        manifest.setLatestQualityCheckScreenshotPath(composition.getLatestQualityCheckScreenshotPath());
        manifest.setLatestQualityCheckTime(composition.getLatestQualityCheckTime());
        manifest.setGenerationResults(buildManifestGenerationResults(composition.getProductDetailId()));
        manifest.setToolchain(buildManifestToolchain(composition));
        manifest.setGeneratedAt(composition.getLatestQualityCheckTime() != null
                ? composition.getLatestQualityCheckTime()
                : composition.getUpdateTime());
        return manifest;
    }

    private void submitAsync(Long jobId) {
        Runnable task = () -> executeComposition(jobId);
        if (taskExecutor == null) {
            task.run();
            return;
        }
        taskExecutor.execute(task);
    }

    private void submitQualityCheckAsync(Long qualityCheckId) {
        Runnable task = () -> executeQualityCheck(qualityCheckId);
        if (taskExecutor == null) {
            task.run();
            return;
        }
        taskExecutor.execute(task);
    }

    private void executeComposition(Long id) {
        DetailComposition job = requireJob(id);
        if (isTerminal(job.getStatus())) {
            return;
        }

        job.setStatus(TaskStatus.RUNNING.getCode());
        job.setProgress(10);
        job.setUpdateTime(now());
        this.updateById(job);

        ToolAdapterInfoDTO tool = resolveTool(job.getToolCode());
        if (tool == null || !tool.isConfigured()) {
            cancel(job, "Tool adapter not configured: " + job.getToolCode());
            return;
        }

        try {
            ToolInvokeRequestDTO request = new ToolInvokeRequestDTO();
            request.setOperation("compose");
            request.setPayload(buildInvokePayload(job));

            ToolInvokeResponseDTO response = toolAdapterService.invoke(job.getToolCode(), request);
            String externalJobId = extractExternalJobId(response);
            if (StringUtils.hasText(externalJobId)) {
                job.setExternalJobId(externalJobId.trim());
            }

            String outputPath = extractOutputPath(response);
            if (!StringUtils.hasText(outputPath)) {
                fail(job, "Tool adapter response did not include an output path");
                return;
            }

            String normalizedOutputPath = outputPath.trim();
            Path verifiedOutputPath = LocalPathPolicy.requirePathWithinRoots(
                    normalizedOutputPath,
                    allowedOutputRoots(),
                    "detail composition output");
            if (!verifiedOutputPath.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png")) {
                fail(job, "Detail composition output must be a PNG file");
                return;
            }
            if (!FileUtil.fileExists(verifiedOutputPath.toString())) {
                fail(job, "Detail composition output file does not exist: " + normalizedOutputPath);
                return;
            }

            ResponseMetadata responseMetadata = extractResponseMetadata(response);
            ImageMetadata imageMetadata = readImageMetadata(verifiedOutputPath);
            if (responseMetadata.fileSize() != null && !responseMetadata.fileSize().equals(imageMetadata.fileSize())) {
                fail(job, "Tool adapter fileSize does not match the output file");
                return;
            }
            if (responseMetadata.width() != null && !responseMetadata.width().equals(imageMetadata.width())) {
                fail(job, "Tool adapter width does not match the output file");
                return;
            }
            if (responseMetadata.height() != null && !responseMetadata.height().equals(imageMetadata.height())) {
                fail(job, "Tool adapter height does not match the output file");
                return;
            }
            upsertResult(job.getId(), verifiedOutputPath, imageMetadata);
            job.setOutputPath(verifiedOutputPath.toString());
            job.setStatus(TaskStatus.SUCCEEDED.getCode());
            job.setProgress(100);
            job.setErrorMessage(null);
            job.setUpdateTime(now());
            this.updateById(job);
        } catch (UnsupportedOperationException | IllegalStateException e) {
            cancel(job, e.getMessage());
        } catch (RuntimeException e) {
            fail(job, e.getMessage());
        }
    }

    private void cancel(DetailComposition job, String reason) {
        job.setStatus(TaskStatus.CANCELED.getCode());
        job.setProgress(0);
        job.setErrorMessage(reason);
        job.setUpdateTime(now());
        this.updateById(job);
    }

    private void fail(DetailComposition job, String reason) {
        job.setStatus(TaskStatus.FAILED.getCode());
        job.setProgress(0);
        job.setErrorMessage(reason);
        job.setUpdateTime(now());
        this.updateById(job);
    }

    private void executeQualityCheck(Long id) {
        DetailCompositionQualityCheck check = requireQualityCheck(id);
        if (isTerminal(check.getStatus())) {
            return;
        }

        DetailComposition job = requireJob(check.getDetailCompositionId());
        if (!TaskStatus.SUCCEEDED.getCode().equals(normalizeStatus(job.getStatus()))) {
            failQualityCheck(check, "Detail composition output is not ready for QA");
            return;
        }

        ToolAdapterInfoDTO tool = resolveTool(check.getToolCode());
        if (tool == null || !tool.isConfigured()) {
            cancelQualityCheck(check, "Tool adapter not configured: " + check.getToolCode());
            return;
        }

        try {
            File outputFile = resolveDownloadFile(job.getId());
            Path outputPath = outputFile.toPath().toAbsolutePath();
            Path screenshotPath = buildQualityCheckScreenshotPath(job.getId(), check.getId());
            ensureOutputDirectory(screenshotPath.toString());

            ToolInvokeRequestDTO request = new ToolInvokeRequestDTO();
            request.setOperation(DEFAULT_QA_OPERATION);
            request.setPayload(buildQualityCheckPayload(job, outputPath, screenshotPath));

            ToolInvokeResponseDTO response = toolAdapterService.invoke(check.getToolCode(), request);
            QualityCheckEvaluation evaluation = evaluateQualityCheckResponse(response, screenshotPath);
            if (!evaluation.issues().isEmpty()) {
                failQualityCheck(check, "Quality check reported issues", evaluation);
                return;
            }

            check.setStatus(TaskStatus.SUCCEEDED.getCode());
            check.setIssueCount(evaluation.issueCount());
            check.setIssuesJson(writeJson(evaluation.issues()));
            check.setScreenshotPath(evaluation.screenshotPath().toString());
            check.setErrorMessage(null);
            check.setCheckTime(now());
            check.setUpdateTime(now());
            detailCompositionQualityCheckMapper.updateById(check);
        } catch (UnsupportedOperationException e) {
            cancelQualityCheck(check, e.getMessage());
        } catch (RuntimeException e) {
            failQualityCheck(check, e.getMessage());
        }
    }

    private void cancelQualityCheck(DetailCompositionQualityCheck check, String reason) {
        check.setStatus(TaskStatus.CANCELED.getCode());
        check.setIssueCount(0);
        check.setIssuesJson(writeJson(List.of()));
        check.setErrorMessage(reason);
        check.setCheckTime(now());
        check.setUpdateTime(now());
        detailCompositionQualityCheckMapper.updateById(check);
    }

    private void failQualityCheck(DetailCompositionQualityCheck check, String reason) {
        failQualityCheck(check, reason, new QualityCheckEvaluation(null, 0, List.of()));
    }

    private void failQualityCheck(DetailCompositionQualityCheck check, String reason, QualityCheckEvaluation evaluation) {
        check.setStatus(TaskStatus.FAILED.getCode());
        check.setIssueCount(evaluation.issueCount());
        check.setIssuesJson(writeJson(evaluation.issues()));
        check.setScreenshotPath(evaluation.screenshotPath() == null ? null : evaluation.screenshotPath().toString());
        check.setErrorMessage(reason);
        check.setCheckTime(now());
        check.setUpdateTime(now());
        detailCompositionQualityCheckMapper.updateById(check);
    }

    private Long upsertResult(Long compositionId, Path outputPath, ImageMetadata imageMetadata) {
        String normalizedOutputPath = outputPath.toString();
        DetailCompositionResult existing = this.detailCompositionResultMapper.selectOne(
                new LambdaQueryWrapper<DetailCompositionResult>()
                        .eq(DetailCompositionResult::getDetailCompositionId, compositionId)
                        .eq(DetailCompositionResult::getOutputPath, normalizedOutputPath)
                        .last("limit 1"));

        File file = outputPath.toFile();
        if (existing == null) {
            DetailCompositionResult result = new DetailCompositionResult();
            result.setDetailCompositionId(compositionId);
            result.setOutputPath(normalizedOutputPath);
            result.setFileName(file.getName());
            result.setFileSize(imageMetadata.fileSize());
            result.setImageWidth(imageMetadata.width());
            result.setImageHeight(imageMetadata.height());
            result.setMimeType(DEFAULT_MIME_TYPE);
            result.setCreateTime(now());
            result.setUpdateTime(now());
            this.detailCompositionResultMapper.insert(result);
            return result.getId();
        }

        existing.setFileName(file.getName());
        existing.setFileSize(imageMetadata.fileSize());
        existing.setImageWidth(imageMetadata.width());
        existing.setImageHeight(imageMetadata.height());
        existing.setMimeType(DEFAULT_MIME_TYPE);
        existing.setUpdateTime(now());
        this.detailCompositionResultMapper.updateById(existing);
        return existing.getId();
    }

    private DetailComposition requireJob(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("composition id must not be null");
        }
        DetailComposition job = this.getById(id);
        if (job == null) {
            throw new IllegalStateException("detail composition not found: " + id);
        }
        return job;
    }

    private ProductDetail requireDetail(Long id) {
        ProductDetail detail = productDetailMapper.selectById(id);
        if (detail == null) {
            throw new IllegalStateException("product detail not found: " + id);
        }
        return detail;
    }

    private DetailCompositionDTO toDTO(DetailComposition job) {
        DetailCompositionDTO dto = new DetailCompositionDTO();
        dto.setId(job.getId());
        dto.setProductDetailId(job.getProductDetailId());
        dto.setTaskName(job.getTaskName());
        dto.setToolCode(job.getToolCode());
        dto.setInputData(readMap(job.getInputJson()));
        dto.setModuleOrder(readModuleOrder(job.getInputJson()));
        dto.setStatus(job.getStatus());
        dto.setProgress(job.getProgress());
        dto.setExternalJobId(job.getExternalJobId());
        dto.setOutputPath(job.getOutputPath());
        fillResult(dto, job.getId());
        fillLatestQualityCheck(dto, job);
        dto.setErrorMessage(job.getErrorMessage());
        dto.setCreateTime(job.getCreateTime());
        dto.setUpdateTime(job.getUpdateTime());
        return dto;
    }

    private void fillResult(DetailCompositionDTO dto, Long jobId) {
        DetailCompositionResult result = this.detailCompositionResultMapper.selectOne(
                new LambdaQueryWrapper<DetailCompositionResult>()
                        .eq(DetailCompositionResult::getDetailCompositionId, jobId)
                        .orderByDesc(DetailCompositionResult::getCreateTime)
                        .last("limit 1"));
        if (result == null) {
            return;
        }
        dto.setOutputPath(result.getOutputPath() != null ? result.getOutputPath() : dto.getOutputPath());
        dto.setOutputFileName(result.getFileName());
        dto.setOutputFileSize(result.getFileSize());
        dto.setOutputWidth(result.getImageWidth());
        dto.setOutputHeight(result.getImageHeight());
        dto.setMimeType(result.getMimeType());
    }

    private void fillLatestQualityCheck(DetailCompositionDTO dto, DetailComposition job) {
        DetailCompositionQualityCheck check = loadLatestQualityCheck(job.getId());
        if (check != null) {
            dto.setLatestQualityCheckStatus(check.getStatus());
            dto.setLatestQualityCheckIssueCount(check.getIssueCount());
            dto.setLatestQualityCheckScreenshotPath(check.getScreenshotPath());
            dto.setLatestQualityCheckTime(check.getCheckTime());
        }
        dto.setDeliverable(isDeliverable(job, check));
    }

    private Map<String, Object> buildInputSnapshot(DetailCompositionCreateDTO dto, ProductDetail detail) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        ProductDetailDTO detailData = dto.getDetailData();
        if (detailData == null) {
            detailData = convertToDTO(detail);
        }

        snapshot.put("productDetailId", dto.getProductDetailId());
        snapshot.put("taskName", resolveTaskName(dto, detail));
        snapshot.put("toolCode", resolveToolCode(dto));
        snapshot.put("detailData", detailData);
        snapshot.put("moduleOrder", dto.getModuleOrder() == null || dto.getModuleOrder().isEmpty()
                ? detailData.getModuleOrder()
                : dto.getModuleOrder());
        snapshot.put("productSnapshot", convertToSnapshotMap(detail));
        return snapshot;
    }

    private Map<String, Object> buildInvokePayload(DetailComposition job) {
        Map<String, Object> payload = new LinkedHashMap<>(readMap(job.getInputJson()));
        List<String> inputImages = extractInputImages(payload);
        if (inputImages.isEmpty()) {
            throw new IllegalArgumentException("detail composition requires at least one real input image");
        }
        payload.put("compositionId", job.getId());
        payload.put("productDetailId", job.getProductDetailId());
        payload.put("toolCode", job.getToolCode());
        payload.put("outputPath", job.getOutputPath());
        payload.put("outputRatio", resolveOutputRatio(payload));
        payload.put("inputImages", inputImages);
        return payload;
    }

    private Map<String, Object> buildQualityCheckPayload(DetailComposition job, Path outputPath, Path screenshotPath) {
        ImageMetadata outputMetadata = readImageMetadata(outputPath);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("detailCompositionId", job.getId());
        payload.put("productDetailId", job.getProductDetailId());
        payload.put("outputPath", outputPath.toString());
        payload.put("outputFileUri", LocalPathPolicy.toFileUri(outputPath));
        payload.put("screenshotPath", screenshotPath.toString());
        payload.put("expectedWidth", outputMetadata.width());
        payload.put("expectedHeight", outputMetadata.height());
        payload.put("checks", List.of("screenshot", "dimensions", "blank", "readability"));
        return payload;
    }

    private QualityCheckEvaluation evaluateQualityCheckResponse(ToolInvokeResponseDTO response, Path requestedScreenshotPath) {
        if (response == null || !(response.getBody() instanceof Map<?, ?> body)) {
            throw new IllegalStateException("Tool adapter response body is malformed");
        }

        Object screenshotValue = firstNonNull(body, "screenshotPath", "filePath", "path");
        if (screenshotValue == null && body.get("data") instanceof Map<?, ?> nested) {
            screenshotValue = firstNonNull(nested, "screenshotPath", "filePath", "path");
        }
        if (screenshotValue == null) {
            throw new IllegalStateException("Tool adapter response did not include a screenshot path");
        }
        String screenshot = String.valueOf(screenshotValue).trim();
        Path screenshotPath = LocalPathPolicy.requirePathWithinRoots(screenshot, allowedOutputRoots(), "quality-check screenshot");
        if (!Files.isRegularFile(screenshotPath) || !Files.isReadable(screenshotPath)) {
            throw new IllegalStateException("Quality-check screenshot file does not exist: " + screenshotPath);
        }
        try {
            if (Files.size(screenshotPath) <= 0) {
                throw new IllegalStateException("Quality-check screenshot file is empty: " + screenshotPath);
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read quality-check screenshot file: " + screenshotPath, e);
        }
        readImageMetadata(screenshotPath);

        List<String> issues = readIssues(body);
        Integer issueCount = firstInteger(body, "issueCount");
        if (issueCount == null) {
            issueCount = issues.size();
        }
        if (issueCount != issues.size()) {
            issueCount = issues.size();
        }
        return new QualityCheckEvaluation(screenshotPath, issueCount, issues);
    }

    private List<DetailCompositionQualityCheck> loadQualityChecks(Long compositionId) {
        if (detailCompositionQualityCheckMapper == null) {
            return List.of();
        }
        List<DetailCompositionQualityCheck> all = detailCompositionQualityCheckMapper.selectList(new LambdaQueryWrapper<>());
        if (all == null || all.isEmpty()) {
            return List.of();
        }
        List<DetailCompositionQualityCheck> filtered = new ArrayList<>();
        for (DetailCompositionQualityCheck check : all) {
            if (check != null && compositionId.equals(check.getDetailCompositionId())) {
                filtered.add(check);
            }
        }
        filtered.sort((left, right) -> {
            LocalDateTime leftTime = left.getCheckTime() != null ? left.getCheckTime() : left.getCreateTime();
            LocalDateTime rightTime = right.getCheckTime() != null ? right.getCheckTime() : right.getCreateTime();
            if (leftTime == null && rightTime == null) {
                return 0;
            }
            if (leftTime == null) {
                return 1;
            }
            if (rightTime == null) {
                return -1;
            }
            return rightTime.compareTo(leftTime);
        });
        return filtered;
    }

    private DetailCompositionQualityCheck loadLatestQualityCheck(Long compositionId) {
        List<DetailCompositionQualityCheck> checks = loadQualityChecks(compositionId);
        return checks.isEmpty() ? null : checks.get(0);
    }

    private DetailCompositionQualityCheck requireQualityCheck(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("quality check id must not be null");
        }
        if (detailCompositionQualityCheckMapper == null) {
            throw new IllegalStateException("Detail composition quality-check persistence is not available");
        }
        DetailCompositionQualityCheck check = detailCompositionQualityCheckMapper.selectById(id);
        if (check == null) {
            throw new IllegalStateException("detail composition quality check not found: " + id);
        }
        return check;
    }

    private DetailCompositionQualityCheckDTO toQualityCheckDTO(DetailCompositionQualityCheck check) {
        DetailCompositionQualityCheckDTO dto = new DetailCompositionQualityCheckDTO();
        dto.setId(check.getId());
        dto.setDetailCompositionId(check.getDetailCompositionId());
        dto.setToolCode(check.getToolCode());
        dto.setStatus(check.getStatus());
        dto.setIssueCount(check.getIssueCount());
        dto.setIssues(readStringList(check.getIssuesJson()));
        dto.setScreenshotPath(check.getScreenshotPath());
        dto.setErrorMessage(check.getErrorMessage());
        dto.setCheckTime(check.getCheckTime());
        dto.setCreateTime(check.getCreateTime());
        dto.setUpdateTime(check.getUpdateTime());
        return dto;
    }

    private List<Map<String, Object>> buildManifestGenerationResults(Long productDetailId) {
        if (detailGenerationResultLinkMapper == null) {
            return List.of();
        }
        List<DetailGenerationResultLink> links = detailGenerationResultLinkMapper.selectList(new LambdaQueryWrapper<>());
        if (links == null || links.isEmpty()) {
            return List.of();
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (DetailGenerationResultLink link : links) {
            if (link == null || !productDetailId.equals(link.getProductDetailId())) {
                continue;
            }
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("generationResultId", link.getGenerationResultId());
            record.put("resultUrl", link.getResultUrl());
            GenerationResult generationResult = generationResultMapper == null || link.getGenerationResultId() == null
                    ? null
                    : generationResultMapper.selectById(link.getGenerationResultId());
            if (generationResult != null) {
                record.put("imageJobId", generationResult.getImageJobId());
                record.put("thumbnailUrl", generationResult.getThumbnailUrl());
                record.put("prompt", generationResult.getPrompt());
                record.put("paramsJson", generationResult.getParamsJson());
                record.put("complianceStatus", generationResult.getComplianceStatus());
                record.put("selected", generationResult.getSelected());
            }
            records.add(record);
        }
        return records;
    }

    private List<String> buildManifestToolchain(DetailCompositionDTO composition) {
        LinkedHashSet<String> tools = new LinkedHashSet<>();
        if (StringUtils.hasText(composition.getToolCode())) {
            tools.add(composition.getToolCode());
        }
        if (StringUtils.hasText(composition.getLatestQualityCheckStatus())) {
            tools.add(DEFAULT_QA_TOOL_CODE);
        }
        return new ArrayList<>(tools);
    }

    private boolean isDeliverable(DetailComposition job, DetailCompositionQualityCheck latestCheck) {
        if (job == null || latestCheck == null) {
            return false;
        }
        if (!TaskStatus.SUCCEEDED.getCode().equals(normalizeStatus(job.getStatus()))) {
            return false;
        }
        if (!TaskStatus.SUCCEEDED.getCode().equals(normalizeStatus(latestCheck.getStatus()))) {
            return false;
        }
        if (!StringUtils.hasText(job.getOutputPath())) {
            return false;
        }
        try {
            return resolveDownloadFile(job.getId()).exists();
        } catch (RuntimeException e) {
            return false;
        }
    }

    private Path buildQualityCheckScreenshotPath(Long compositionId, Long qualityCheckId) {
        return Path.of(DEFAULT_QA_DIR, "composition-" + compositionId + "-qa-" + qualityCheckId + ".png")
                .toAbsolutePath()
                .normalize();
    }

    private List<String> readIssues(Map<?, ?> body) {
        Object issuesValue = body.get("issues");
        if (issuesValue == null && body.get("data") instanceof Map<?, ?> nested) {
            issuesValue = nested.get("issues");
        }
        if (issuesValue == null) {
            throw new IllegalStateException("Tool adapter response did not include issues");
        }
        return toStringList(issuesValue);
    }

    private String buildOutputPath(Long jobId, ProductDetail detail) {
        String safeName = StringUtils.hasText(detail.getTitle()) ? detail.getTitle().trim() : "detail-composition";
        safeName = safeName.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_");
        if (safeName.length() > 40) {
            safeName = safeName.substring(0, 40);
        }
        return DEFAULT_OUTPUT_DIR + "/" + safeName + "-" + jobId + ".png";
    }

    private void ensureOutputDirectory(String outputPath) {
        if (!StringUtils.hasText(outputPath)) {
            return;
        }
        try {
            Path parent = Paths.get(outputPath).toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (Exception e) {
            throw new IllegalStateException("failed to prepare detail composition output directory", e);
        }
    }

    private String resolveTaskName(DetailCompositionCreateDTO dto, ProductDetail detail) {
        if (StringUtils.hasText(dto.getTaskName())) {
            return dto.getTaskName().trim();
        }
        if (StringUtils.hasText(detail.getTitle())) {
            return detail.getTitle().trim() + " detail composition";
        }
        if (StringUtils.hasText(detail.getProductName())) {
            return detail.getProductName().trim() + " detail composition";
        }
        return "detail composition";
    }

    private String resolveToolCode(DetailCompositionCreateDTO dto) {
        return StringUtils.hasText(dto.getToolCode()) ? dto.getToolCode().trim() : DEFAULT_TOOL_CODE;
    }

    private List<String> extractInputImages(Map<String, Object> payload) {
        Object detailData = payload.get("detailData");
        if (detailData instanceof Map<?, ?> detailDataMap) {
            Object images = detailDataMap.get("images");
            return toStringList(images);
        }
        Object productSnapshot = payload.get("productSnapshot");
        if (productSnapshot instanceof Map<?, ?> snapshotMap) {
            Object images = snapshotMap.get("images");
            return toStringList(images);
        }
        return List.of();
    }

    private String resolveOutputRatio(Map<String, Object> payload) {
        Object value = payload.get("outputRatio");
        if (value != null && StringUtils.hasText(String.valueOf(value))) {
            return String.valueOf(value).trim();
        }
        return DEFAULT_OUTPUT_RATIO;
    }

    private ToolAdapterInfoDTO resolveTool(String toolCode) {
        if (toolAdapterService == null) {
            return null;
        }
        return toolAdapterService.getTool(toolCode);
    }

    private boolean isTerminal(String status) {
        String normalized = normalizeStatus(status);
        return TaskStatus.SUCCEEDED.getCode().equals(normalized)
                || TaskStatus.FAILED.getCode().equals(normalized)
                || TaskStatus.CANCELED.getCode().equals(normalized);
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return "";
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private String extractExternalJobId(ToolInvokeResponseDTO response) {
        if (response == null || response.getBody() == null) {
            return null;
        }
        Object body = response.getBody();
        if (body instanceof Map<?, ?> map) {
            Object value = firstNonNull(map, "prompt_id", "promptId", "job_id", "jobId", "externalJobId", "id");
            return value == null ? null : String.valueOf(value);
        }
        if (body instanceof String text && StringUtils.hasText(text)) {
            return text.trim();
        }
        return response.getRawBody();
    }

    private String extractOutputPath(ToolInvokeResponseDTO response) {
        if (response == null) {
            return null;
        }
        Object body = response.getBody();
        if (body instanceof Map<?, ?> map) {
            Object value = firstNonNull(map, "outputPath", "filePath", "resultPath", "path", "output");
            if (value == null) {
                Object data = map.get("data");
                if (data instanceof Map<?, ?> nested) {
                    value = firstNonNull(nested, "outputPath", "filePath", "resultPath", "path", "output");
                }
            }
            return value == null ? null : String.valueOf(value);
        }
        if (body instanceof String text && StringUtils.hasText(text)) {
            return text.trim();
        }
        return StringUtils.hasText(response.getRawBody()) ? response.getRawBody().trim() : null;
    }

    private ResponseMetadata extractResponseMetadata(ToolInvokeResponseDTO response) {
        if (response == null || response.getBody() == null) {
            return new ResponseMetadata(null, null, null);
        }
        Object body = response.getBody();
        if (body instanceof Map<?, ?> map) {
            Long fileSize = firstLong(map, "fileSize", "size");
            Integer width = firstInteger(map, "width", "imageWidth");
            Integer height = firstInteger(map, "height", "imageHeight");
            Object data = map.get("data");
            if (data instanceof Map<?, ?> nested) {
                fileSize = firstNonNullLong(fileSize, firstLong(nested, "fileSize", "size"));
                width = firstNonNullInteger(width, firstInteger(nested, "width", "imageWidth"));
                height = firstNonNullInteger(height, firstInteger(nested, "height", "imageHeight"));
            }
            return new ResponseMetadata(fileSize, width, height);
        }
        return new ResponseMetadata(null, null, null);
    }

    private Object firstNonNull(Map<?, ?> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null && StringUtils.hasText(String.valueOf(value))) {
                return value;
            }
        }
        return null;
    }

    private Long firstLong(Map<?, ?> map, String... keys) {
        Object value = firstNonNull(map, keys);
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer firstInteger(Map<?, ?> map, String... keys) {
        Object value = firstNonNull(map, keys);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long firstNonNullLong(Long current, Long next) {
        return current != null ? current : next;
    }

    private Integer firstNonNullInteger(Integer current, Integer next) {
        return current != null ? current : next;
    }

    private Map<String, Object> readMap(String json) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("failed to parse detail composition json", e);
        }
    }

    private List<String> readModuleOrder(String json) {
        Map<String, Object> map = readMap(json);
        Object moduleOrder = map.get("moduleOrder");
        if (moduleOrder instanceof List<?> list) {
            List<String> values = new ArrayList<>();
            for (Object item : list) {
                if (item != null && StringUtils.hasText(String.valueOf(item))) {
                    values.add(String.valueOf(item).trim());
                }
            }
            return values;
        }
        return List.of();
    }

    private Map<String, Object> convertToSnapshotMap(ProductDetail detail) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", detail.getId());
        snapshot.put("materialId", detail.getMaterialId());
        snapshot.put("productName", detail.getProductName());
        snapshot.put("brandId", detail.getBrandId());
        snapshot.put("brandName", detail.getBrandName());
        snapshot.put("title", detail.getTitle());
        snapshot.put("subtitle", detail.getSubtitle());
        snapshot.put("sellingPoints", parseList(detail.getSellingPoints()));
        snapshot.put("seoKeywords", parseList(detail.getSeoKeywords()));
        snapshot.put("moduleOrder", parseList(detail.getModuleOrder()));
        snapshot.put("imageTemplateId", detail.getImageTemplateId());
        snapshot.put("sku", detail.getSku());
        snapshot.put("category", detail.getCategory());
        snapshot.put("price", detail.getPrice());
        snapshot.put("description", detail.getDescription());
        snapshot.put("aiGeneratedContent", detail.getAiGeneratedContent());
        snapshot.put("images", parseList(detail.getImages()));
        snapshot.put("videos", parseList(detail.getVideos()));
        snapshot.put("documents", parseList(detail.getDocuments()));
        snapshot.put("riskLevel", detail.getRiskLevel());
        snapshot.put("riskDescription", detail.getRiskDescription());
        snapshot.put("auditStatus", detail.getAuditStatus());
        snapshot.put("auditComment", detail.getAuditComment());
        return snapshot;
    }

    private ProductDetailDTO convertToDTO(ProductDetail detail) {
        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setProductId(detail.getId());
        dto.setMaterialId(detail.getMaterialId());
        dto.setBrandId(detail.getBrandId());
        dto.setProductName(detail.getProductName() != null ? detail.getProductName() : detail.getTitle());
        dto.setTitle(detail.getTitle());
        dto.setSubtitle(detail.getSubtitle());
        dto.setCategory(detail.getCategory());
        dto.setSku(detail.getSku());
        dto.setPrice(detail.getPrice());
        dto.setDescription(detail.getDescription());
        dto.setAiGeneratedContent(detail.getAiGeneratedContent());
        dto.setCreator(detail.getCreator());
        dto.setSellingPoints(parseList(detail.getSellingPoints()));
        dto.setSeoKeywords(parseList(detail.getSeoKeywords()));
        dto.setModuleOrder(parseList(detail.getModuleOrder()));
        dto.setImages(parseList(detail.getImages()));
        dto.setVideos(parseList(detail.getVideos()));
        dto.setDocuments(parseList(detail.getDocuments()));
        return dto;
    }

    private List<String> parseList(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            List<String> values = new ArrayList<>();
            for (String item : value.split("[,\\n]")) {
                if (StringUtils.hasText(item)) {
                    values.add(item.trim());
                }
            }
            return values;
        }
    }

    private List<String> readStringList(String value) {
        return parseList(value);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize detail composition json", e);
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    private List<Path> allowedOutputRoots() {
        String configuredRoots = environment == null
                ? ""
                : environment.getProperty("tools.adapters.imagemagick.allowed-output-roots", "");
        return LocalPathPolicy.parseAllowedRoots(configuredRoots, DEFAULT_OUTPUT_ROOTS);
    }

    private List<String> toStringList(Object value) {
        if (value instanceof List<?> list) {
            List<String> values = new ArrayList<>();
            for (Object item : list) {
                if (item != null && StringUtils.hasText(String.valueOf(item))) {
                    values.add(String.valueOf(item).trim());
                }
            }
            return values;
        }
        if (value instanceof String text && StringUtils.hasText(text)) {
            return parseList(text);
        }
        return List.of();
    }

    private ImageMetadata readImageMetadata(Path outputPath) {
        try {
            if (!Files.isRegularFile(outputPath) || !Files.isReadable(outputPath)) {
                throw new IllegalStateException("Detail composition output file does not exist: " + outputPath);
            }
            long fileSize = Files.size(outputPath);
            if (fileSize <= 0) {
                throw new IllegalStateException("Detail composition output file is empty: " + outputPath);
            }
            BufferedImage image = ImageIO.read(outputPath.toFile());
            if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
                throw new IllegalStateException("Detail composition output file is not a readable image: " + outputPath);
            }
            return new ImageMetadata(fileSize, image.getWidth(), image.getHeight());
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read detail composition output metadata: " + outputPath, e);
        }
    }

    private record ImageMetadata(long fileSize, int width, int height) {
    }

    private record ResponseMetadata(Long fileSize, Integer width, Integer height) {
    }

    private record QualityCheckEvaluation(Path screenshotPath, int issueCount, List<String> issues) {
    }
}
