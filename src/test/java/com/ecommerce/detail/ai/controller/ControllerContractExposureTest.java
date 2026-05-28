package com.ecommerce.detail.ai.controller;

import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControllerContractExposureTest {

    @Test
    void productMaterialControllerExposesListUpdateDelete() {
        assertControllerBasePath(ProductMaterialController.class, "/material");
        assertRoute(ProductMaterialController.class, "listMaterials", RequestMethod.GET, "/list");
        assertRoute(ProductMaterialController.class, "updateMaterial", RequestMethod.PUT, "/{id}");
        assertRoute(ProductMaterialController.class, "deleteMaterial", RequestMethod.DELETE, "/{id}");
    }

    @Test
    void productDetailControllerExposesListUpdateDeleteAndRiskEndpoints() {
        assertControllerBasePath(ProductDetailController.class, "/detail");
        assertRoute(ProductDetailController.class, "listProductDetails", RequestMethod.GET, "/list");
        assertRoute(ProductDetailController.class, "updateProductDetail", RequestMethod.PUT, "/{id}");
        assertRoute(ProductDetailController.class, "deleteProductDetail", RequestMethod.DELETE, "/{id}");
        assertRoute(ProductDetailController.class, "applyGenerationResults", RequestMethod.POST, "/{id}/generation-results/apply");
        assertRoute(ProductDetailController.class, "riskCheckProductDetail", RequestMethod.POST, "/{id}/risk-check");
        assertRoute(ProductDetailController.class, "getRiskResult", RequestMethod.GET, "/{id}/risk");
        assertRoute(ProductDetailController.class, "regenerateProductDetail", RequestMethod.POST, "/{id}/regenerate");
    }

    @Test
    void auditControllerExposesTaskListAndWorkflowActions() {
        assertControllerBasePath(AuditController.class, "/audit");
        assertRoute(AuditController.class, "getAuditById", RequestMethod.GET, "/{id}");
        assertRoute(AuditController.class, "listAuditRecords", RequestMethod.GET, "/list");
        assertRoute(AuditController.class, "approveAudit", RequestMethod.PUT, "/{id}/approve");
        assertRoute(AuditController.class, "rejectAudit", RequestMethod.PUT, "/{id}/reject");
        assertRoute(AuditController.class, "returnAudit", RequestMethod.PUT, "/{id}/return");
        assertRoute(AuditController.class, "withdrawAudit", RequestMethod.PUT, "/{id}/withdraw");
        assertRoute(AuditController.class, "reaudit", RequestMethod.PUT, "/{id}/reaudit");
    }

    @Test
    void exportControllerExposesListDownloadDeleteAndReexport() {
        assertControllerBasePath(ExportController.class, "/export");
        assertRoute(ExportController.class, "listExportRecords", RequestMethod.GET, "/list");
        assertRoute(ExportController.class, "downloadExportFile", RequestMethod.GET, "/{id}/download");
        assertDownloadReturnTypeIsResponseEntity();
        assertRoute(ExportController.class, "deleteExport", RequestMethod.DELETE, "/{id}");
        assertRoute(ExportController.class, "reexport", RequestMethod.POST, "/{id}/reexport");
    }

    @Test
    void researchTaskControllerExposesTaskLifecycleAndCharts() throws Exception {
        Class<?> controllerClass = Class.forName("com.ecommerce.detail.ai.controller.ResearchTaskController");
        assertControllerBasePath(controllerClass, "/research/tasks");
        assertRoute(controllerClass, "listResearchTasks", RequestMethod.GET, "/list");
        assertRoute(controllerClass, "createResearchTask", RequestMethod.POST, "");
        assertRoute(controllerClass, "getResearchTaskById", RequestMethod.GET, "/{id}");
        assertRoute(controllerClass, "updateResearchTaskStatus", RequestMethod.PUT, "/{id}/status");
        assertRoute(controllerClass, "updateResearchTaskResult", RequestMethod.PUT, "/{id}/result");
        assertRoute(controllerClass, "getResearchTaskCharts", RequestMethod.GET, "/{id}/charts");
    }

    @Test
    void assetOcrTaskControllerExposesTaskLifecycle() throws Exception {
        Class<?> controllerClass = Class.forName("com.ecommerce.detail.ai.controller.AssetOcrTaskController");
        assertControllerBasePath(controllerClass, "/assets/ocr-tasks");
        assertRoute(controllerClass, "listAssetOcrTasks", RequestMethod.GET, "/list");
        assertRoute(controllerClass, "createAssetOcrTask", RequestMethod.POST, "");
        assertRoute(controllerClass, "getAssetOcrTaskById", RequestMethod.GET, "/{id}");
        assertRoute(controllerClass, "updateAssetOcrTaskStatus", RequestMethod.PUT, "/{id}/status");
        assertRoute(controllerClass, "updateAssetOcrTaskResult", RequestMethod.PUT, "/{id}/result");
    }

    @Test
    void designDraftControllerExposesCreateReadUpdateAndList() throws Exception {
        Class<?> controllerClass = Class.forName("com.ecommerce.detail.ai.controller.DesignDraftController");
        assertControllerBasePath(controllerClass, "/design-drafts");
        assertRoute(controllerClass, "listDesignDrafts", RequestMethod.GET, "/list");
        assertRoute(controllerClass, "createDesignDraft", RequestMethod.POST, "");
        assertRoute(controllerClass, "getDesignDraftById", RequestMethod.GET, "/{id}");
        assertRoute(controllerClass, "updateDesignDraft", RequestMethod.PUT, "/{id}");
    }

    @Test
    void categoryVisualPolicyControllerExposesCrudRoutes() throws Exception {
        Class<?> controllerClass = Class.forName("com.ecommerce.detail.ai.controller.CategoryVisualPolicyController");
        assertControllerBasePath(controllerClass, "/category-visual-policies");
        assertRoute(controllerClass, "listCategoryVisualPolicies", RequestMethod.GET, "");
        assertRoute(controllerClass, "listCategoryVisualPolicies", RequestMethod.GET, "/list");
        assertRoute(controllerClass, "createCategoryVisualPolicy", RequestMethod.POST, "");
        assertRoute(controllerClass, "getCategoryVisualPolicyById", RequestMethod.GET, "/{id}");
        assertRoute(controllerClass, "updateCategoryVisualPolicy", RequestMethod.PUT, "/{id}");
        assertRoute(controllerClass, "confirmCategoryVisualPolicy", RequestMethod.POST, "/{id}/confirm");
    }

    @Test
    void modelProfileControllerExposesCrudRoutes() throws Exception {
        Class<?> controllerClass = Class.forName("com.ecommerce.detail.ai.controller.ModelProfileController");
        assertControllerBasePath(controllerClass, "/model-profiles");
        assertRoute(controllerClass, "listModelProfiles", RequestMethod.GET, "");
        assertRoute(controllerClass, "listModelProfiles", RequestMethod.GET, "/list");
        assertRoute(controllerClass, "createModelProfile", RequestMethod.POST, "");
        assertRoute(controllerClass, "getModelProfileById", RequestMethod.GET, "/{id}");
        assertRoute(controllerClass, "updateModelProfile", RequestMethod.PUT, "/{id}");
        assertRoute(controllerClass, "confirmModelProfile", RequestMethod.POST, "/{id}/confirm");
    }

    @Test
    void skcPolicyControllerExposesCrudRoutes() throws Exception {
        Class<?> controllerClass = Class.forName("com.ecommerce.detail.ai.controller.SkcPolicyController");
        assertControllerBasePath(controllerClass, "/skc-policies");
        assertRoute(controllerClass, "listSkcPolicies", RequestMethod.GET, "/list");
        assertRoute(controllerClass, "createSkcPolicy", RequestMethod.POST, "");
        assertRoute(controllerClass, "getSkcPolicyById", RequestMethod.GET, "/{id}");
        assertRoute(controllerClass, "updateSkcPolicy", RequestMethod.PUT, "/{id}");
        assertRoute(controllerClass, "confirmSkcPolicy", RequestMethod.POST, "/{id}/confirm");
    }

    @Test
    void promptWorkbenchControllerExposesPromptRoutes() throws Exception {
        Class<?> controllerClass = Class.forName("com.ecommerce.detail.ai.controller.PromptWorkbenchController");
        assertControllerBasePath(controllerClass, "/prompt-workbench");
        assertRoute(controllerClass, "listPromptWorkbenchEntries", RequestMethod.GET, "");
        assertRoute(controllerClass, "listPromptWorkbenchEntries", RequestMethod.GET, "/list");
        assertRoute(controllerClass, "getPromptWorkbenchEntryById", RequestMethod.GET, "/{id}");
        assertRoute(controllerClass, "createGuidedPrompt", RequestMethod.POST, "/guided");
        assertRoute(controllerClass, "expandPrompt", RequestMethod.POST, "/expand");
        assertRoute(controllerClass, "imageToPrompt", RequestMethod.POST, "/image-to-prompt");
    }

    @Test
    void visualPlanControllerExposesPlanRoutes() throws Exception {
        Class<?> controllerClass = Class.forName("com.ecommerce.detail.ai.controller.VisualPlanController");
        assertControllerBasePath(controllerClass, "/visual-plans");
        assertRoute(controllerClass, "listVisualPlans", RequestMethod.GET, "");
        assertRoute(controllerClass, "listVisualPlans", RequestMethod.GET, "/list");
        assertRoute(controllerClass, "createVisualPlan", RequestMethod.POST, "");
        assertRoute(controllerClass, "getVisualPlanById", RequestMethod.GET, "/{id}");
        assertRoute(controllerClass, "updateVisualPlan", RequestMethod.PUT, "/{id}");
        assertRoute(controllerClass, "confirmVisualPlan", RequestMethod.POST, "/{id}/confirm");
        assertRoute(controllerClass, "dispatchFromVisualPlan", RequestMethod.POST, "/{id}/dispatch");
        assertRoute(controllerClass, "getBatchStatus", RequestMethod.GET, "/{id}/batch-status");
    }

    @Test
    void productContentTaskControllerExposesTaskLifecycleAndApply() throws Exception {
        Class<?> controllerClass = Class.forName("com.ecommerce.detail.ai.controller.ProductContentTaskController");
        assertControllerBasePath(controllerClass, "/product-content-tasks");
        assertRoute(controllerClass, "listProductContentTasks", RequestMethod.GET, "");
        assertRoute(controllerClass, "listProductContentTasks", RequestMethod.GET, "/list");
        assertRoute(controllerClass, "createProductContentTask", RequestMethod.POST, "");
        assertRoute(controllerClass, "getProductContentTaskById", RequestMethod.GET, "/{id}");
        assertRoute(controllerClass, "applyProductContentTask", RequestMethod.POST, "/{id}/apply");
    }

    @Test
    void detailCompositionControllerExposesCreateReadAndList() throws Exception {
        Class<?> controllerClass = Class.forName("com.ecommerce.detail.ai.controller.DetailCompositionController");
        assertControllerBasePath(controllerClass, "/detail-compositions");
        assertRoute(controllerClass, "listDetailCompositions", RequestMethod.GET, "/list");
        assertRoute(controllerClass, "createDetailComposition", RequestMethod.POST, "");
        assertRoute(controllerClass, "getDetailCompositionById", RequestMethod.GET, "/{id}");
        assertRoute(controllerClass, "createQualityCheck", RequestMethod.POST, "/{id}/quality-checks");
        assertRoute(controllerClass, "listQualityChecks", RequestMethod.GET, "/{id}/quality-checks/list");
        assertRoute(controllerClass, "getDeliveryManifest", RequestMethod.GET, "/{id}/delivery-manifest");
    }

    @Test
    void imageJobControllerExposesTaskLifecycle() throws Exception {
        Class<?> controllerClass = Class.forName("com.ecommerce.detail.ai.controller.ImageJobController");
        assertControllerBasePath(controllerClass, "/image-jobs");
        assertRoute(controllerClass, "listImageJobs", RequestMethod.GET, "/list");
        assertRoute(controllerClass, "createImageJob", RequestMethod.POST, "");
        assertRoute(controllerClass, "getImageJobById", RequestMethod.GET, "/{id}");
        assertRoute(controllerClass, "updateImageJobStatus", RequestMethod.PUT, "/{id}/status");
        assertRoute(controllerClass, "retryImageJob", RequestMethod.POST, "/{id}/retry");
        assertRoute(controllerClass, "cancelImageJob", RequestMethod.POST, "/{id}/cancel");
    }

    @Test
    void generationResultControllerExposesListReadAndSelectionUpdate() throws Exception {
        Class<?> controllerClass = Class.forName("com.ecommerce.detail.ai.controller.GenerationResultController");
        assertControllerBasePath(controllerClass, "/generation-results");
        assertRoute(controllerClass, "listGenerationResults", RequestMethod.GET, "/list");
        assertRoute(controllerClass, "getGenerationResultById", RequestMethod.GET, "/{id}");
        assertRoute(controllerClass, "updateGenerationResultSelection", RequestMethod.PUT, "/{id}/selection");
    }

    @Test
    void detailCompositionControllerExposesCreateReadListAndDownload() throws Exception {
        Class<?> controllerClass = Class.forName("com.ecommerce.detail.ai.controller.DetailCompositionController");
        assertControllerBasePath(controllerClass, "/detail-compositions");
        assertRoute(controllerClass, "listDetailCompositions", RequestMethod.GET, "/list");
        assertRoute(controllerClass, "createDetailComposition", RequestMethod.POST, "");
        assertRoute(controllerClass, "getDetailCompositionById", RequestMethod.GET, "/{id}");
        assertRoute(controllerClass, "createQualityCheck", RequestMethod.POST, "/{id}/quality-checks");
        assertRoute(controllerClass, "listQualityChecks", RequestMethod.GET, "/{id}/quality-checks/list");
        assertRoute(controllerClass, "getDeliveryManifest", RequestMethod.GET, "/{id}/delivery-manifest");
        assertRoute(controllerClass, "downloadDetailCompositionFile", RequestMethod.GET, "/{id}/download");
        assertDownloadReturnTypeIsResponseEntity(controllerClass, "downloadDetailCompositionFile");
    }

    @Test
    void productDetailControllerExposesModuleOrderEndpoints() {
        assertRoute(ProductDetailController.class, "getModuleOrder", RequestMethod.GET, "/{id}/module-order");
        assertRoute(ProductDetailController.class, "updateModuleOrder", RequestMethod.PUT, "/{id}/module-order");
    }

    @Test
    void controllersDoNotExposeApiPrefix() {
        assertNoApiPrefix(ProductMaterialController.class);
        assertNoApiPrefix(ProductDetailController.class);
        assertNoApiPrefix(AuditController.class);
        assertNoApiPrefix(ExportController.class);
        assertNoApiPrefix(loadControllerClass("com.ecommerce.detail.ai.controller.ResearchTaskController"));
        assertNoApiPrefix(loadControllerClass("com.ecommerce.detail.ai.controller.AssetOcrTaskController"));
        assertNoApiPrefix(loadControllerClass("com.ecommerce.detail.ai.controller.DesignDraftController"));
        assertNoApiPrefix(loadControllerClass("com.ecommerce.detail.ai.controller.DetailCompositionController"));
        assertNoApiPrefix(loadControllerClass("com.ecommerce.detail.ai.controller.ImageJobController"));
        assertNoApiPrefix(loadControllerClass("com.ecommerce.detail.ai.controller.GenerationResultController"));
        assertNoApiPrefix(loadControllerClass("com.ecommerce.detail.ai.controller.CategoryVisualPolicyController"));
        assertNoApiPrefix(loadControllerClass("com.ecommerce.detail.ai.controller.ModelProfileController"));
        assertNoApiPrefix(loadControllerClass("com.ecommerce.detail.ai.controller.SkcPolicyController"));
        assertNoApiPrefix(loadControllerClass("com.ecommerce.detail.ai.controller.PromptWorkbenchController"));
        assertNoApiPrefix(loadControllerClass("com.ecommerce.detail.ai.controller.VisualPlanController"));
        assertNoApiPrefix(loadControllerClass("com.ecommerce.detail.ai.controller.ProductContentTaskController"));
    }

    private void assertControllerBasePath(Class<?> controllerClass, String expectedPath) {
        RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(controllerClass, RequestMapping.class);
        assertNotNull(mapping, controllerClass.getSimpleName() + " missing class-level request mapping");
        assertTrue(Arrays.asList(mapping.value()).contains(expectedPath),
                controllerClass.getSimpleName() + " should map to " + expectedPath);
        assertNoApiPrefix(mapping.value(), controllerClass.getSimpleName() + " class-level mapping");
    }

    private void assertNoApiPrefix(Class<?> controllerClass) {
        RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(controllerClass, RequestMapping.class);
        assertNotNull(mapping, controllerClass.getSimpleName() + " missing class-level request mapping");
        assertNoApiPrefix(mapping.value(), controllerClass.getSimpleName() + " class-level mapping");
    }

    private Class<?> loadControllerClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Missing controller class " + className, e);
        }
    }

    private void assertNoApiPrefix(String[] paths, String scope) {
        assertTrue(Arrays.stream(paths).noneMatch(path -> path != null && path.startsWith("/api")),
                scope + " should not be prefixed with /api");
    }

    private void assertRoute(Class<?> controllerClass, String methodName, RequestMethod httpMethod, String path) {
        Method method = Arrays.stream(controllerClass.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(controllerClass.getSimpleName() + " missing method " + methodName));

        RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
        assertNotNull(mapping, controllerClass.getSimpleName() + "." + methodName + " missing request mapping");
        assertTrue(Arrays.asList(mapping.method()).contains(httpMethod),
                controllerClass.getSimpleName() + "." + methodName + " should expose " + httpMethod);
        assertTrue(Arrays.asList(mapping.value()).contains(path),
                controllerClass.getSimpleName() + "." + methodName + " should map to " + path);
        assertNoApiPrefix(mapping.value(), controllerClass.getSimpleName() + "." + methodName + " method mapping");

        if (httpMethod == RequestMethod.GET) {
            assertNotNull(AnnotatedElementUtils.findMergedAnnotation(method, GetMapping.class));
        } else if (httpMethod == RequestMethod.POST) {
            assertNotNull(AnnotatedElementUtils.findMergedAnnotation(method, PostMapping.class));
        } else if (httpMethod == RequestMethod.PUT) {
            assertNotNull(AnnotatedElementUtils.findMergedAnnotation(method, PutMapping.class));
        } else if (httpMethod == RequestMethod.DELETE) {
            assertNotNull(AnnotatedElementUtils.findMergedAnnotation(method, DeleteMapping.class));
        }
    }

    private void assertDownloadReturnTypeIsResponseEntity() {
        Method method = Arrays.stream(ExportController.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals("downloadExportFile"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("ExportController missing method downloadExportFile"));

        assertTrue(ResponseEntity.class.isAssignableFrom(method.getReturnType()),
                "ExportController.downloadExportFile should return a ResponseEntity for real file download");
    }

    private void assertDownloadReturnTypeIsResponseEntity(Class<?> controllerClass, String methodName) {
        Method method = Arrays.stream(controllerClass.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(controllerClass.getSimpleName() + " missing method " + methodName));

        assertTrue(ResponseEntity.class.isAssignableFrom(method.getReturnType()),
                controllerClass.getSimpleName() + "." + methodName + " should return a ResponseEntity for real file download");
    }


    @Test
    void publishCheckControllerExposesRunListSummaryAndOverride() {
        assertControllerBasePath(PublishCheckController.class, "/publish-checks");
        assertRoute(PublishCheckController.class, "runChecks", RequestMethod.POST, "/run/{productDetailId}");
        assertRoute(PublishCheckController.class, "listChecks", RequestMethod.GET, "/list/{productDetailId}");
        assertRoute(PublishCheckController.class, "getSummary", RequestMethod.GET, "/summary/{productDetailId}");
        assertRoute(PublishCheckController.class, "overrideCheck", RequestMethod.POST, "/{checkId}/override");
    }
}
