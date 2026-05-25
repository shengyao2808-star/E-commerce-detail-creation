package com.ecommerce.detail.ai.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExportUtilTest {

    @Test
    void exportProductDetailByIdDoesNotGeneratePlaceholderContent() {
        ExportUtil exportUtil = new ExportUtil(new FileUtil());

        assertThrows(UnsupportedOperationException.class,
                () -> exportUtil.exportProductDetailById(1L, "HTML"));
    }

    @Test
    void exportProductDetailDoesNotDowngradePdfToText() {
        ExportUtil exportUtil = new ExportUtil(new FileUtil());
        com.ecommerce.detail.ai.dto.ProductDetailDTO dto = new com.ecommerce.detail.ai.dto.ProductDetailDTO();
        dto.setProductName("测试商品");
        dto.setAiGeneratedContent("测试详情页内容");

        assertThrows(UnsupportedOperationException.class,
                () -> exportUtil.exportProductDetail(dto, null, "PDF"));
    }
    @Test
    void exportProductDetailSupportsTxtFormat() {
        ExportUtil exportUtil = new ExportUtil(new FileUtil());
        com.ecommerce.detail.ai.dto.ProductDetailDTO dto = new com.ecommerce.detail.ai.dto.ProductDetailDTO();
        dto.setProductName("test-product");
        dto.setDescription("test description");

        String path = exportUtil.exportProductDetail(dto, null, "TXT");

        assertTrue(path.endsWith(".txt"));
    }
}
