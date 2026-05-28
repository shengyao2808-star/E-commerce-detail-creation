package com.ecommerce.detail.ai.service.impl;

import com.ecommerce.detail.ai.dto.DetailRiskResultDTO;
import com.ecommerce.detail.ai.entity.ProductDetail;
import com.ecommerce.detail.ai.mapper.ProductDetailMapper;
import com.ecommerce.detail.ai.util.RiskCheckUtil;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductDetailServiceImplTest {

    @Test
    void getModuleOrderReturnsEmptyListWhenPersistedValueMissing() {
        ProductDetail detail = new ProductDetail();
        detail.setId(6L);
        detail.setTitle("模块顺序为空");
        detail.setModuleOrder(null);

        RecordingProductDetailMapper mapper = new RecordingProductDetailMapper(detail);
        ProductDetailServiceImpl service = service(mapper, new RecordingRiskCheckUtil(riskResult("LOW", false, List.of(), Map.of(), List.of())));

        List<String> moduleOrder = service.getModuleOrder(6L);

        assertEquals(List.of(), moduleOrder);
        assertEquals(0, mapper.updateCount);
    }

    @Test
    void updateModuleOrderPersistsJsonArray() {
        ProductDetail detail = new ProductDetail();
        detail.setId(10L);
        detail.setTitle("模块顺序持久化");

        RecordingProductDetailMapper mapper = new RecordingProductDetailMapper(detail);
        ProductDetailServiceImpl service = service(mapper, new RecordingRiskCheckUtil(riskResult("LOW", false, List.of(), Map.of(), List.of())));

        boolean updated = service.updateModuleOrder(10L, List.of("标题", "卖点", "图片"));

        assertTrue(updated);
        assertEquals(1, mapper.updateCount);
        assertEquals("[\"标题\",\"卖点\",\"图片\"]", mapper.updatedDetail.getModuleOrder());
    }

    @Test
    void checkProductDetailRiskPersistsRiskResultAndReturnsDto() {
        ProductDetail detail = new ProductDetail();
        detail.setId(7L);
        detail.setProductName("测试商品");
        detail.setTitle("标题包含风险词");
        detail.setSubtitle("副标题");
        detail.setSellingPoints("最佳,保湿");
        detail.setSeoKeywords("护肤,最佳");
        detail.setCategory("美妆");
        detail.setSku("SKU-7");
        detail.setDescription("描述内容");
        detail.setAiGeneratedContent("AI详情内容");
        detail.setAuditStatus(0);
        detail.setAuditComment("待人工审核");

        RecordingProductDetailMapper mapper = new RecordingProductDetailMapper(detail);
        RecordingRiskCheckUtil riskCheckUtil = new RecordingRiskCheckUtil(riskResult(
                "HIGH",
                true,
                List.of("[广告法禁用词] 最佳"),
                Map.of("advertisement", List.of("[广告法禁用词] 最佳")),
                List.of("请移除广告法禁用词")));
        ProductDetailServiceImpl service = service(mapper, riskCheckUtil);

        DetailRiskResultDTO dto = service.checkProductDetailRisk(7L);

        assertEquals(7L, dto.getProductDetailId());
        assertEquals("HIGH", dto.getRiskLevel());
        assertTrue(dto.isHasRisk());
        assertEquals(List.of("[广告法禁用词] 最佳"), dto.getIssues());
        assertEquals(List.of("请移除广告法禁用词"), dto.getSuggestions());
        assertTrue(dto.getContent().contains("title: 标题包含风险词"));
        assertTrue(dto.getContent().contains("sellingPoints: 最佳,保湿"));
        assertEquals(0, dto.getAuditStatus());
        assertEquals("待人工审核", dto.getAuditComment());
        assertNotNull(dto.getUpdateTime());

        assertEquals(1, mapper.updateCount);
        assertEquals("HIGH", mapper.updatedDetail.getRiskLevel());
        assertEquals("[广告法禁用词] 最佳", mapper.updatedDetail.getRiskDescription());
        assertNotNull(mapper.updatedDetail.getUpdateTime());
    }

    @Test
    void getProductDetailRiskDoesNotRecheckWhenRiskLevelAlreadyExists() {
        ProductDetail detail = new ProductDetail();
        detail.setId(8L);
        detail.setTitle("已检测标题");
        detail.setRiskLevel("MEDIUM");
        detail.setRiskDescription("[广告法禁用词] 最佳");
        detail.setAuditStatus(1);
        detail.setAuditComment("需要修改");

        RecordingProductDetailMapper mapper = new RecordingProductDetailMapper(detail);
        RecordingRiskCheckUtil riskCheckUtil = new RecordingRiskCheckUtil(riskResult(
                "HIGH",
                true,
                List.of("[广告法禁用词] 最佳"),
                Map.of("advertisement", List.of("[广告法禁用词] 最佳")),
                List.of("请移除广告法禁用词")));
        ProductDetailServiceImpl service = service(mapper, riskCheckUtil);

        DetailRiskResultDTO dto = service.getProductDetailRisk(8L);

        assertEquals(8L, dto.getProductDetailId());
        assertEquals("MEDIUM", dto.getRiskLevel());
        assertTrue(dto.isHasRisk());
        assertEquals(List.of("[广告法禁用词] 最佳"), dto.getIssues());
        assertEquals(List.of("请根据风险描述调整商品详情内容，并提交人工复核"), dto.getSuggestions());
        assertEquals(1, dto.getAuditStatus());
        assertEquals("需要修改", dto.getAuditComment());
        assertEquals(0, riskCheckUtil.checkCount);
        assertEquals(0, mapper.updateCount);
    }

    @Test
    void getProductDetailRiskRunsCheckWhenRiskLevelIsMissing() {
        ProductDetail detail = new ProductDetail();
        detail.setId(9L);
        detail.setTitle("未检测标题");
        detail.setAiGeneratedContent("普通详情内容");

        RecordingProductDetailMapper mapper = new RecordingProductDetailMapper(detail);
        RecordingRiskCheckUtil riskCheckUtil = new RecordingRiskCheckUtil(riskResult(
                "LOW",
                false,
                List.of(),
                Map.of(),
                List.of("未发现明显风险，建议人工复核确认")));
        ProductDetailServiceImpl service = service(mapper, riskCheckUtil);

        DetailRiskResultDTO dto = service.getProductDetailRisk(9L);

        assertEquals(9L, dto.getProductDetailId());
        assertEquals("LOW", dto.getRiskLevel());
        assertFalse(dto.isHasRisk());
        assertEquals(List.of(), dto.getIssues());
        assertEquals(List.of("未发现明显风险，建议人工复核确认"), dto.getSuggestions());
        assertTrue(dto.getContent().contains("title: 未检测标题"));
        assertEquals(1, riskCheckUtil.checkCount);
        assertEquals(1, mapper.updateCount);
        assertEquals("LOW", mapper.updatedDetail.getRiskLevel());
        assertEquals("未发现明显风险", mapper.updatedDetail.getRiskDescription());
    }

    private static ProductDetailServiceImpl service(RecordingProductDetailMapper mapper,
                                                    RecordingRiskCheckUtil riskCheckUtil) {
        ProductDetailServiceImpl service = new ProductDetailServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper.proxy());
        ReflectionTestUtils.setField(service, "riskCheckUtil", riskCheckUtil);
        return service;
    }

    private static RiskCheckUtil.RiskCheckResult riskResult(String riskLevel,
                                                           boolean hasRisk,
                                                           List<String> issues,
                                                           Map<String, List<String>> issueDetails,
                                                           List<String> suggestions) {
        RiskCheckUtil.RiskCheckResult result = new RiskCheckUtil.RiskCheckResult();
        result.setRiskLevel(riskLevel);
        result.setHasRisk(hasRisk);
        result.setIssues(issues);
        result.setIssueDetails(issueDetails);
        result.setSuggestions(suggestions);
        return result;
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

    private static class RecordingProductDetailMapper {
        private final ProductDetail detail;
        private ProductDetail updatedDetail;
        private int updateCount;

        private RecordingProductDetailMapper(ProductDetail detail) {
            this.detail = detail;
        }

        private ProductDetailMapper proxy() {
            return (ProductDetailMapper) Proxy.newProxyInstance(
                    ProductDetailMapper.class.getClassLoader(),
                    new Class<?>[]{ProductDetailMapper.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();
                        if ("selectById".equals(methodName)) {
                            Serializable id = (Serializable) args[0];
                            return detail.getId().equals(id) ? detail : null;
                        }
                        if ("updateById".equals(methodName)) {
                            updatedDetail = (ProductDetail) args[0];
                            updateCount++;
                            return 1;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }

    private static class RecordingRiskCheckUtil extends RiskCheckUtil {
        private final RiskCheckResult result;
        private int checkCount;

        private RecordingRiskCheckUtil(RiskCheckResult result) {
            this.result = result;
        }

        @Override
        public RiskCheckResult checkRisk(String content) {
            checkCount++;
            result.setContent(content);
            return result;
        }
    }
}
