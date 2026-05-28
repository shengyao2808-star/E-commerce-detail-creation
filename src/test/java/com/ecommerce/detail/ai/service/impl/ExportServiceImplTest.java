package com.ecommerce.detail.ai.service.impl;

import com.ecommerce.detail.ai.dto.ExportDTO;
import com.ecommerce.detail.ai.dto.ProductDetailDTO;
import com.ecommerce.detail.ai.entity.ExportRecord;
import com.ecommerce.detail.ai.entity.ProductDetail;
import com.ecommerce.detail.ai.mapper.ExportRecordMapper;
import com.ecommerce.detail.ai.mapper.ProductDetailMapper;
import com.ecommerce.detail.ai.util.ExportUtil;
import com.ecommerce.detail.ai.util.FileUtil;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ExportServiceImplTest {

    @Test
    void exportProductDetailUsesPersistedProductDetailContent() {
        ExportServiceImpl service = new ExportServiceImpl();

        ExportRecordMapper exportRecordMapper = proxy(ExportRecordMapper.class, invocation -> {
            String methodName = invocation.method().getName();
            if ("insert".equals(methodName)) {
                ExportRecord record = (ExportRecord) invocation.args()[0];
                record.setId(99L);
                return 1;
            }
            if ("updateById".equals(methodName)) {
                return 1;
            }
            return defaultValue(invocation.method().getReturnType());
        });

        ProductDetail detail = new ProductDetail();
        detail.setId(7L);
        detail.setMaterialId(3L);
        detail.setBrandId(5L);
        detail.setProductName("real-product");
        detail.setTitle("real-title");
        detail.setSubtitle("real-subtitle");
        detail.setSku("SKU-REAL");
        detail.setCategory("home");
        detail.setPrice(new BigDecimal("199.00"));
        detail.setDescription("real description");
        detail.setAiGeneratedContent("real ai content");
        detail.setAuditStatus(2);

        ProductDetailMapper productDetailMapper = proxy(ProductDetailMapper.class, invocation -> {
            if ("selectById".equals(invocation.method().getName())) {
                return detail;
            }
            return defaultValue(invocation.method().getReturnType());
        });

        RecordingExportUtil exportUtil = new RecordingExportUtil();

        ReflectionTestUtils.setField(service, "baseMapper", exportRecordMapper);
        ReflectionTestUtils.setField(service, "productDetailMapper", productDetailMapper);
        ReflectionTestUtils.setField(service, "exportUtil", exportUtil);

        ExportDTO dto = new ExportDTO();
        dto.setProductDetailId(7L);
        dto.setExportFormat("HTML");
        dto.setExporter("tester");

        Long exportId = service.exportProductDetail(dto);

        assertEquals(99L, exportId);
        assertFalse(exportUtil.exportByIdCalled);
        assertNotNull(exportUtil.capturedDto);
        assertEquals("real-product", exportUtil.capturedDto.getProductName());
        assertEquals("real-title", exportUtil.capturedDto.getTitle());
        assertEquals("real ai content", exportUtil.capturedDto.getAiGeneratedContent());
    }

    @Test
    void exportProductDetailRejectsPdfAsNotImplemented() {
        ExportServiceImpl service = new ExportServiceImpl();
        ExportDTO dto = new ExportDTO();
        dto.setProductDetailId(7L);
        dto.setExportFormat("pdf");
        dto.setExporter("tester");

        UnsupportedOperationException exception =
                assertThrows(UnsupportedOperationException.class, () -> service.exportProductDetail(dto));
        assertTrue(exception.getMessage().toLowerCase().contains("pdf"));
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> invocation.invoke(new InvocationContext(method, args)));
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

    @FunctionalInterface
    private interface Invocation {
        Object invoke(InvocationContext invocation) throws Throwable;
    }

    private record InvocationContext(java.lang.reflect.Method method, Object[] args) {
    }

    private static class RecordingExportUtil extends ExportUtil {
        private ProductDetailDTO capturedDto;
        private boolean exportByIdCalled;

        private RecordingExportUtil() {
            super(new FileUtil());
        }

        @Override
        public String exportProductDetail(ProductDetailDTO dto,
                                          com.ecommerce.detail.ai.entity.BrandTemplate template,
                                          String format) {
            this.capturedDto = dto;
            return "exports/real.html";
        }

        @Override
        public String exportProductDetailById(Long productDetailId, String format) {
            this.exportByIdCalled = true;
            return "exports/stub.html";
        }
    }
}
