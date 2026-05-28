package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.ApplyGenerationResultsDTO;
import com.ecommerce.detail.ai.dto.DetailRiskResultDTO;
import com.ecommerce.detail.ai.dto.ProductDetailDTO;
import com.ecommerce.detail.ai.entity.BrandTemplate;
import com.ecommerce.detail.ai.entity.DetailGenerationResultLink;
import com.ecommerce.detail.ai.entity.GenerationResult;
import com.ecommerce.detail.ai.entity.ProductDetail;
import com.ecommerce.detail.ai.mapper.BrandTemplateMapper;
import com.ecommerce.detail.ai.mapper.DetailGenerationResultLinkMapper;
import com.ecommerce.detail.ai.mapper.GenerationResultMapper;
import com.ecommerce.detail.ai.mapper.ProductDetailMapper;
import com.ecommerce.detail.ai.service.ProductDetailService;
import com.ecommerce.detail.ai.util.AIUtil;
import com.ecommerce.detail.ai.util.ExportUtil;
import com.ecommerce.detail.ai.util.RiskCheckUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 商品详情页服务实现类
 *
 * @author Administrator
 * @version 1.0.0
 */
@Slf4j
@Service
public class ProductDetailServiceImpl extends ServiceImpl<ProductDetailMapper, ProductDetail> implements ProductDetailService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<List<String>>() {};

    @Autowired(required = false)
    private ObjectMapper objectMapper = OBJECT_MAPPER;

    @Autowired
    private BrandTemplateMapper brandTemplateMapper;

    @Autowired
    private AIUtil aiUtil;

    @Autowired
    private ExportUtil exportUtil;

    @Autowired
    private RiskCheckUtil riskCheckUtil;

    @Autowired(required = false)
    private GenerationResultMapper generationResultMapper;

    @Autowired(required = false)
    private DetailGenerationResultLinkMapper detailGenerationResultLinkMapper;

    @Override
    public Long generateProductDetail(ProductDetailDTO dto) {
        // 参数校验
        if (dto == null || dto.getMaterialId() == null) {
            throw new IllegalArgumentException("商品资料ID不能为空");
        }

        // 获取品牌模板（如果有）
        BrandTemplate template = null;
        if (dto.getBrandId() != null && dto.getImageTemplateId() != null) {
            template = brandTemplateMapper.selectById(dto.getImageTemplateId());
        }

        // AI生成内容
        String productInfo = buildProductInfo(dto);
        String aiContent = aiUtil.generateDetailContent(productInfo, template != null ? template.getStyleDescription() : "标准风格");

        // 风险检测
        com.ecommerce.detail.ai.util.RiskCheckUtil.RiskCheckResult riskResult = riskCheckUtil.checkRisk(aiContent);
        String riskLevel = riskResult.getRiskLevel();

        ProductDetail detail = new ProductDetail();
        detail.setMaterialId(dto.getMaterialId());
        detail.setBrandId(dto.getBrandId());
        detail.setTitle(dto.getTitle());
        detail.setSubtitle(dto.getSubtitle());
        detail.setSellingPoints(dto.getSellingPoints() != null ? String.join(",", dto.getSellingPoints()) : null);
        detail.setSeoKeywords(dto.getSeoKeywords() != null ? String.join(",", dto.getSeoKeywords()) : null);
        detail.setModuleOrder(serializeModuleOrder(dto.getModuleOrder()));
        detail.setImageTemplateId(dto.getImageTemplateId());
        detail.setAiGeneratedContent(aiContent);
        detail.setRiskLevel(riskLevel);
        detail.setAuditStatus(0); // 待审核
        detail.setCreator(dto.getCreator());
        detail.setCreateTime(LocalDateTime.now());
        detail.setUpdateTime(LocalDateTime.now());

        this.save(detail);
        log.info("AI生成商品详情页成功，ID: {}, 标题: {}, 风险等级: {}", detail.getId(), detail.getTitle(), riskLevel);
        return detail.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchGenerateProductDetails(List<ProductDetailDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (ProductDetailDTO dto : dtos) {
            try {
                generateProductDetail(dto);
                successCount++;
            } catch (Exception e) {
                log.error("批量生成商品详情页失败，商品资料ID: {}", dto.getMaterialId(), e);
            }
        }

        log.info("批量生成完成，总数: {}, 成功: {}", dtos.size(), successCount);
        return successCount;
    }

    @Override
    public ProductDetail getProductDetailById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID不能为空");
        }

        ProductDetail detail = this.getById(id);
        if (detail == null) {
            throw new RuntimeException("商品详情页不存在，ID: " + id);
        }

        return detail;
    }

    @Override
    public PageResult<ProductDetail> listProductDetails(int pageNum, int pageSize, String keyword, Integer status) {
        // 参数校验和默认值设置
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        Page<ProductDetail> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProductDetail> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(ProductDetail::getTitle, keyword)
                   .or()
                   .like(ProductDetail::getSubtitle, keyword);
        }

        // 状态筛选
        if (status != null) {
            wrapper.eq(ProductDetail::getAuditStatus, status);
        }

        // 按创建时间倒序排列
        wrapper.orderByDesc(ProductDetail::getCreateTime);

        Page<ProductDetail> resultPage = this.page(page, wrapper);

        return new PageResult<>(resultPage.getTotal(), resultPage.getRecords(), pageNum, pageSize);
    }

    @Override
    public boolean updateProductDetail(Long id, ProductDetailDTO dto) {
        if (id == null || dto == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        ProductDetail existing = this.getById(id);
        if (existing == null) {
            throw new RuntimeException("商品详情页不存在，ID: " + id);
        }

        // 更新字段
        if (dto.getTitle() != null) {
            existing.setTitle(dto.getTitle());
        }
        if (dto.getSubtitle() != null) {
            existing.setSubtitle(dto.getSubtitle());
        }
        if (dto.getSellingPoints() != null) {
            existing.setSellingPoints(String.join(",", dto.getSellingPoints()));
        }
        if (dto.getSeoKeywords() != null) {
            existing.setSeoKeywords(String.join(",", dto.getSeoKeywords()));
        }
        if (dto.getImageTemplateId() != null) {
            existing.setImageTemplateId(dto.getImageTemplateId());
        }
        if (dto.getAiGeneratedContent() != null) {
            existing.setAiGeneratedContent(dto.getAiGeneratedContent());
        }
        if (dto.getModuleOrder() != null) {
            existing.setModuleOrder(serializeModuleOrder(dto.getModuleOrder()));
        }

        existing.setUpdateTime(LocalDateTime.now());

        boolean result = this.updateById(existing);
        log.info("更新商品详情页{}，ID: {}", result ? "成功" : "失败", id);
        return result;
    }

    @Override
    public List<String> getModuleOrder(Long id) {
        ProductDetail detail = getProductDetailById(id);
        return parseModuleOrder(detail.getModuleOrder());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateModuleOrder(Long id, List<String> moduleOrder) {
        ProductDetail detail = getProductDetailById(id);
        detail.setModuleOrder(serializeModuleOrder(moduleOrder));
        detail.setUpdateTime(LocalDateTime.now());
        return this.updateById(detail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int applyGenerationResults(Long id, ApplyGenerationResultsDTO dto) {
        if (generationResultMapper == null || detailGenerationResultLinkMapper == null) {
            throw new IllegalStateException("Generation result persistence is not available");
        }

        ProductDetail detail = getProductDetailById(id);
        List<GenerationResult> selectedResults = resolveGenerationResults(dto);
        if (selectedResults.isEmpty()) {
            throw new IllegalArgumentException("No selected generation results are available to apply");
        }

        LinkedHashSet<String> mergedImages = new LinkedHashSet<>(parseStringList(detail.getImages()));
        int appliedCount = 0;
        for (GenerationResult result : selectedResults) {
            String resultUrl = normalizeText(result.getResultUrl());
            if (!StringUtils.hasText(resultUrl)) {
                throw new IllegalArgumentException("Generation result resultUrl must not be blank");
            }
            mergedImages.add(resultUrl);
            upsertGenerationResultLink(detail.getId(), result.getId(), resultUrl);
            appliedCount++;
        }

        detail.setImages(writeStringList(new ArrayList<>(mergedImages)));
        detail.setUpdateTime(LocalDateTime.now());
        this.updateById(detail);
        return appliedCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProductDetail(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID不能为空");
        }

        ProductDetail detail = this.getById(id);
        if (detail == null) {
            throw new RuntimeException("商品详情页不存在，ID: " + id);
        }

        boolean result = this.removeById(id);
        log.info("删除商品详情页{}，ID: {}", result ? "成功" : "失败", id);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteProductDetails(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        int deleteCount = 0;
        for (Long id : ids) {
            try {
                if (deleteProductDetail(id)) {
                    deleteCount++;
                }
            } catch (Exception e) {
                log.error("批量删除商品详情页失败，ID: {}", id, e);
            }
        }

        log.info("批量删除完成，总数: {}, 成功: {}", ids.size(), deleteCount);
        return deleteCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditProductDetail(Long id, Boolean approved, String comment) {
        if (id == null || approved == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        ProductDetail detail = this.getById(id);
        if (detail == null) {
            throw new RuntimeException("商品详情页不存在，ID: " + id);
        }

        // 更新审核状态
        detail.setAuditStatus(approved ? 1 : 2); // 1-通过，2-拒绝
        detail.setAuditComment(comment);
        detail.setAuditor("system"); // 实际应从SecurityContext获取
        detail.setAuditTime(LocalDateTime.now());
        detail.setUpdateTime(LocalDateTime.now());

        boolean result = this.updateById(detail);
        log.info("审核商品详情页{}，ID: {}, 结果: {}", result ? "成功" : "失败", id, approved ? "通过" : "拒绝");
        return result;
    }

    @Override
    public boolean regenerateProductDetail(Long id) {
        ProductDetail detail = getProductDetailById(id);

        // 构建DTO
        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setMaterialId(detail.getMaterialId());
        dto.setBrandId(detail.getBrandId());
        dto.setTitle(detail.getTitle());
        dto.setSubtitle(detail.getSubtitle());
        if (detail.getSellingPoints() != null) {
            dto.setSellingPoints(java.util.Arrays.asList(detail.getSellingPoints().split(",")));
        }
        if (detail.getSeoKeywords() != null) {
            dto.setSeoKeywords(java.util.Arrays.asList(detail.getSeoKeywords().split(",")));
        }
        dto.setImageTemplateId(detail.getImageTemplateId());

        // 重新生成
        String aiContent = aiUtil.generateProductDetail(dto, null);
        com.ecommerce.detail.ai.util.RiskCheckUtil.RiskCheckResult riskResult = riskCheckUtil.checkRisk(aiContent);

        detail.setAiGeneratedContent(aiContent);
        detail.setRiskLevel(riskResult.getRiskLevel());
        detail.setAuditStatus(0); // 重置为待审核
        detail.setUpdateTime(LocalDateTime.now());

        boolean result = this.updateById(detail);
        log.info("重新生成商品详情页{}，ID: {}, 风险等级: {}", result ? "成功" : "失败", id, riskResult.getRiskLevel());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DetailRiskResultDTO checkProductDetailRisk(Long id) {
        ProductDetail detail = getProductDetailById(id);
        String content = buildRiskCheckContent(detail);
        RiskCheckUtil.RiskCheckResult riskResult = riskCheckUtil.checkRisk(content);
        LocalDateTime now = LocalDateTime.now();
        String riskDescription = buildRiskDescription(riskResult);

        detail.setRiskLevel(riskResult.getRiskLevel());
        detail.setRiskDescription(riskDescription);
        detail.setUpdateTime(now);
        this.updateById(detail);

        detail.setUpdateTime(now);
        detail.setRiskDescription(riskDescription);
        return buildRiskResultDTO(detail, riskResult);
    }

    @Override
    public DetailRiskResultDTO getProductDetailRisk(Long id) {
        ProductDetail detail = getProductDetailById(id);
        if (isBlank(detail.getRiskLevel())) {
            return checkProductDetailRisk(id);
        }
        return buildRiskResultDTO(detail, null);
    }

    @Override
    public String exportProductDetail(Long id, String format) {
        ProductDetail detail = getProductDetailById(id);

        // 验证格式
        if (!"word".equalsIgnoreCase(format) &&
            !"docx".equalsIgnoreCase(format) &&
            !"markdown".equalsIgnoreCase(format) &&
            !"md".equalsIgnoreCase(format) &&
            !"json".equalsIgnoreCase(format) &&
            !"html".equalsIgnoreCase(format) &&
            !"htm".equalsIgnoreCase(format) &&
            !"txt".equalsIgnoreCase(format) &&
            !"text".equalsIgnoreCase(format)) {
            throw new IllegalArgumentException("不支持的导出格式: " + format);
        }

        // 执行导出
        ProductDetailDTO dto = convertToDTO(detail);
        String filePath = exportUtil.exportProductDetail(dto, null, format);
        log.info("导出商品详情页成功，ID: {}, 格式: {}, 路径: {}", id, format, filePath);
        return filePath;
    }

    /**
     * 构建商品信息字符串用于AI生成
     */
    private String buildProductInfo(ProductDetailDTO dto) {
        StringBuilder sb = new StringBuilder();
        if (dto.getTitle() != null) {
            sb.append("标题：").append(dto.getTitle()).append("\n");
        }
        if (dto.getSubtitle() != null) {
            sb.append("副标题：").append(dto.getSubtitle()).append("\n");
        }
        if (dto.getSellingPoints() != null && !dto.getSellingPoints().isEmpty()) {
            sb.append("卖点：").append(String.join("、", dto.getSellingPoints())).append("\n");
        }
        if (dto.getSeoKeywords() != null && !dto.getSeoKeywords().isEmpty()) {
            sb.append("SEO关键词：").append(String.join("、", dto.getSeoKeywords())).append("\n");
        }
        if (dto.getDescription() != null) {
            sb.append("描述：").append(dto.getDescription()).append("\n");
        }
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            sb.append("图片数量：").append(dto.getImages().size()).append("张\n");
        }
        if (dto.getVideos() != null && !dto.getVideos().isEmpty()) {
            sb.append("视频数量：").append(dto.getVideos().size()).append("个\n");
        }
        if (dto.getDocuments() != null && !dto.getDocuments().isEmpty()) {
            sb.append("文档数量：").append(dto.getDocuments().size()).append("份\n");
        }
        return sb.toString();
    }

    private String buildRiskCheckContent(ProductDetail detail) {
        StringBuilder sb = new StringBuilder();
        appendRiskField(sb, "productName", detail.getProductName());
        appendRiskField(sb, "brandName", detail.getBrandName());
        appendRiskField(sb, "title", detail.getTitle());
        appendRiskField(sb, "subtitle", detail.getSubtitle());
        appendRiskField(sb, "sellingPoints", detail.getSellingPoints());
        appendRiskField(sb, "seoKeywords", detail.getSeoKeywords());
        appendRiskField(sb, "category", detail.getCategory());
        appendRiskField(sb, "sku", detail.getSku());
        appendRiskField(sb, "description", detail.getDescription());
        appendRiskField(sb, "aiGeneratedContent", detail.getAiGeneratedContent());
        appendRiskField(sb, "images", detail.getImages());
        appendRiskField(sb, "videos", detail.getVideos());
        appendRiskField(sb, "documents", detail.getDocuments());
        return sb.toString();
    }

    private void appendRiskField(StringBuilder sb, String fieldName, Object value) {
        if (value == null) {
            return;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return;
        }
        sb.append(fieldName).append(": ").append(text).append('\n');
    }

    private String buildRiskDescription(RiskCheckUtil.RiskCheckResult riskResult) {
        List<String> issues = riskResult.getIssues();
        if (issues == null || issues.isEmpty()) {
            return "未发现明显风险";
        }
        return String.join("; ", issues);
    }

    private DetailRiskResultDTO buildRiskResultDTO(ProductDetail detail, RiskCheckUtil.RiskCheckResult riskResult) {
        DetailRiskResultDTO dto = new DetailRiskResultDTO();
        dto.setId(detail.getId());
        dto.setProductDetailId(detail.getId());
        dto.setRiskLevel(riskResult != null ? riskResult.getRiskLevel() : detail.getRiskLevel());
        dto.setRiskDescription(detail.getRiskDescription());
        dto.setHasRisk(hasRiskLevel(dto.getRiskLevel()));
        dto.setIssues(riskResult != null ? safeList(riskResult.getIssues()) : riskDescriptionAsIssues(detail.getRiskDescription()));
        dto.setIssueDetails(riskResult != null ? safeMap(riskResult.getIssueDetails()) : Collections.emptyMap());
        dto.setSuggestions(riskResult != null ? safeList(riskResult.getSuggestions()) : buildPersistedRiskSuggestions(dto.isHasRisk()));
        dto.setContent(riskResult != null ? riskResult.getContent() : buildRiskCheckContent(detail));
        dto.setAuditStatus(detail.getAuditStatus());
        dto.setAuditComment(detail.getAuditComment());
        dto.setUpdateTime(detail.getUpdateTime());
        return dto;
    }

    private boolean hasRiskLevel(String riskLevel) {
        return riskLevel != null && !"LOW".equalsIgnoreCase(riskLevel.trim());
    }

    private List<String> riskDescriptionAsIssues(String riskDescription) {
        if (isBlank(riskDescription) || "未发现明显风险".equals(riskDescription.trim())) {
            return Collections.emptyList();
        }
        return java.util.Arrays.asList(riskDescription.split(";\\s*"));
    }

    private List<String> buildPersistedRiskSuggestions(boolean hasRisk) {
        if (hasRisk) {
            return Collections.singletonList("请根据风险描述调整商品详情内容，并提交人工复核");
        }
        return Collections.singletonList("未发现明显风险，建议人工复核确认");
    }

    private List<String> safeList(List<String> values) {
        return values != null ? values : Collections.emptyList();
    }

    private Map<String, List<String>> safeMap(Map<String, List<String>> values) {
        return values != null ? values : Collections.emptyMap();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private List<String> parseModuleOrder(String moduleOrder) {
        if (isBlank(moduleOrder)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(moduleOrder, STRING_LIST_TYPE);
        } catch (Exception e) {
            throw new RuntimeException("解析模块顺序失败", e);
        }
    }

    private List<GenerationResult> resolveGenerationResults(ApplyGenerationResultsDTO dto) {
        List<GenerationResult> persisted = generationResultMapper.selectList(new LambdaQueryWrapper<>());
        if (persisted == null || persisted.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> requestedIds = dto == null || dto.getGenerationResultIds() == null
                ? Collections.emptyList()
                : dto.getGenerationResultIds().stream()
                        .filter(java.util.Objects::nonNull)
                        .distinct()
                        .toList();
        if (!requestedIds.isEmpty()) {
            Set<Long> requested = new LinkedHashSet<>(requestedIds);
            List<GenerationResult> results = new ArrayList<>();
            for (GenerationResult result : persisted) {
                if (result != null && result.getId() != null && requested.contains(result.getId())) {
                    results.add(result);
                }
            }
            return results;
        }

        boolean selectedOnly = dto == null || dto.getSelectedOnly() == null || dto.getSelectedOnly();
        if (!selectedOnly) {
            return Collections.emptyList();
        }

        List<GenerationResult> results = new ArrayList<>();
        for (GenerationResult result : persisted) {
            if (result != null && Integer.valueOf(1).equals(result.getSelected())) {
                results.add(result);
            }
        }
        return results;
    }

    private void upsertGenerationResultLink(Long productDetailId, Long generationResultId, String resultUrl) {
        List<DetailGenerationResultLink> existingLinks = detailGenerationResultLinkMapper.selectList(new LambdaQueryWrapper<>());
        DetailGenerationResultLink existing = null;
        if (existingLinks != null) {
            for (DetailGenerationResultLink candidate : existingLinks) {
                if (candidate != null
                        && productDetailId.equals(candidate.getProductDetailId())
                        && generationResultId.equals(candidate.getGenerationResultId())) {
                    existing = candidate;
                    break;
                }
            }
        }

        if (existing == null) {
            DetailGenerationResultLink link = new DetailGenerationResultLink();
            link.setProductDetailId(productDetailId);
            link.setGenerationResultId(generationResultId);
            link.setResultUrl(resultUrl);
            link.setCreateTime(LocalDateTime.now());
            link.setUpdateTime(LocalDateTime.now());
            detailGenerationResultLinkMapper.insert(link);
            return;
        }

        existing.setResultUrl(resultUrl);
        existing.setUpdateTime(LocalDateTime.now());
        detailGenerationResultLinkMapper.updateById(existing);
    }

    private List<String> parseStringList(String value) {
        if (!StringUtils.hasText(value)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(value, STRING_LIST_TYPE);
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

    private String writeStringList(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? Collections.emptyList() : values);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("淇濆瓨鍒楄〃瀛楁澶辫触", e);
        }
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private String serializeModuleOrder(List<String> moduleOrder) {
        try {
            return objectMapper.writeValueAsString(moduleOrder != null ? moduleOrder : Collections.emptyList());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("保存模块顺序失败", e);
        }
    }

    /**
     * 将ProductDetail实体转换为ProductDetailDTO
     */
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

        // 解析sellingPoints字符串为List
        dto.setSellingPoints(parseStringList(detail.getSellingPoints()));

        // 解析seoKeywords字符串为List
        dto.setSeoKeywords(parseStringList(detail.getSeoKeywords()));

        dto.setModuleOrder(parseModuleOrder(detail.getModuleOrder()));
        dto.setImageTemplateId(detail.getImageTemplateId());
        dto.setAiGeneratedContent(detail.getAiGeneratedContent());

        // 解析images、videos、documents字符串为List（假设它们以逗号分隔）
        dto.setImages(parseStringList(detail.getImages()));
        dto.setVideos(parseStringList(detail.getVideos()));
        dto.setDocuments(parseStringList(detail.getDocuments()));

        return dto;
    }
}
