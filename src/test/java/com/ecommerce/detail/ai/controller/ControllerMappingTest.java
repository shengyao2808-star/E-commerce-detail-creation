package com.ecommerce.detail.ai.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ControllerMappingTest {

    @Test
    void controllerMappingsDoNotDuplicateGlobalApiContextPath() {
        List<Class<?>> controllers = List.of(
                ProductDetailController.class,
                ProductMaterialController.class,
                AuditController.class,
                ExportController.class,
                ToolAdapterController.class
        );

        for (Class<?> controller : controllers) {
            RequestMapping mapping = controller.getAnnotation(RequestMapping.class);
            for (String path : mapping.value()) {
                assertFalse(path.startsWith("/api/"),
                        controller.getSimpleName() + " should rely on the global /api/v1 context path");
            }
        }
    }
}
