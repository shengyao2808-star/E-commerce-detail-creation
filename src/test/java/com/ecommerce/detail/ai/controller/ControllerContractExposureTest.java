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
    void controllersDoNotExposeApiPrefix() {
        assertNoApiPrefix(ProductMaterialController.class);
        assertNoApiPrefix(ProductDetailController.class);
        assertNoApiPrefix(AuditController.class);
        assertNoApiPrefix(ExportController.class);
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
}
