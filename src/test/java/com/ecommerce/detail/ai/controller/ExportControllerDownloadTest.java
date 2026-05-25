package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.entity.ExportRecord;
import com.ecommerce.detail.ai.service.ExportService;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportControllerDownloadTest {

    @Test
    void downloadExportFileReturnsAttachmentResponse() throws Exception {
        File tempFile = File.createTempFile("export-record", ".txt");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("export data");
        }

        ExportRecord record = new ExportRecord();
        record.setId(1L);
        record.setFilePath(tempFile.getAbsolutePath());
        record.setFileName("detail.txt");
        record.setExportStatus(1);
        record.setExportFormat("markdown");

        ExportService exportService = (ExportService) Proxy.newProxyInstance(
                ExportService.class.getClassLoader(),
                new Class<?>[]{ExportService.class},
                (proxy, method, args) -> {
                    if ("downloadExportFile".equals(method.getName())) {
                        return record;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });

        ExportController controller = new ExportController();
        java.lang.reflect.Field field = ExportController.class.getDeclaredField("exportService");
        field.setAccessible(true);
        field.set(controller, exportService);

        ResponseEntity<Resource> response = controller.downloadExportFile(1L);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("detail.txt"));
        assertEquals("text/markdown", response.getHeaders().getContentType().toString());
    }
}
