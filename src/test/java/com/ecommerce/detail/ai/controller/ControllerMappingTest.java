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
                ToolAdapterController.class,
                loadController("com.ecommerce.detail.ai.controller.ResearchTaskController"),
                loadController("com.ecommerce.detail.ai.controller.AssetOcrTaskController"),
                loadController("com.ecommerce.detail.ai.controller.DesignDraftController"),
                loadController("com.ecommerce.detail.ai.controller.DetailCompositionController"),
                loadController("com.ecommerce.detail.ai.controller.ImageJobController"),
                loadController("com.ecommerce.detail.ai.controller.GenerationResultController"),
                loadController("com.ecommerce.detail.ai.controller.CategoryVisualPolicyController"),
                loadController("com.ecommerce.detail.ai.controller.ModelProfileController"),
                loadController("com.ecommerce.detail.ai.controller.SkcPolicyController"),
                loadController("com.ecommerce.detail.ai.controller.PromptWorkbenchController"),
                loadController("com.ecommerce.detail.ai.controller.VisualPlanController"),
                loadController("com.ecommerce.detail.ai.controller.ProductContentTaskController")
        );

        for (Class<?> controller : controllers) {
            RequestMapping mapping = controller.getAnnotation(RequestMapping.class);
            for (String path : mapping.value()) {
                assertFalse(path.startsWith("/api/"),
                        controller.getSimpleName() + " should rely on the global /api/v1 context path");
            }
        }
    }

    private Class<?> loadController(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Missing controller class " + className, e);
        }
    }
}
