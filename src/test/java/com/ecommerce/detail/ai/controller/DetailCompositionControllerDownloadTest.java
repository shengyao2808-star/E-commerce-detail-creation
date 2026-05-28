package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.dto.DetailCompositionDTO;
import com.ecommerce.detail.ai.service.DetailCompositionService;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DetailCompositionControllerDownloadTest {

    @Test
    void downloadDetailCompositionFileReturnsAttachmentResponse() throws Exception {
        Path tempPath = Files.createTempFile("detail-composition", ".png");
        File tempFile = tempPath.toFile();
        tempFile.deleteOnExit();

        DetailCompositionDTO dto = new DetailCompositionDTO();
        dto.setId(1L);
        dto.setStatus("SUCCEEDED");
        dto.setOutputPath(tempFile.getAbsolutePath());
        dto.setOutputFileName("detail.png");
        dto.setOutputFileSize(tempFile.length());
        dto.setMimeType("image/png");

        DetailCompositionService service = (DetailCompositionService) Proxy.newProxyInstance(
                DetailCompositionService.class.getClassLoader(),
                new Class<?>[]{DetailCompositionService.class},
                (proxy, method, args) -> {
                    if ("getDetailCompositionById".equals(method.getName())) {
                        return dto;
                    }
                    if ("resolveDownloadFile".equals(method.getName())) {
                        return tempFile;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });

        DetailCompositionController controller = new DetailCompositionController();
        java.lang.reflect.Field field = DetailCompositionController.class.getDeclaredField("detailCompositionService");
        field.setAccessible(true);
        field.set(controller, service);

        ResponseEntity<Resource> response = controller.downloadDetailCompositionFile(1L);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("detail.png"));
        assertEquals("image/png", response.getHeaders().getContentType().toString());
    }
}
