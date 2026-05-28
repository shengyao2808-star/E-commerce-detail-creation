package com.ecommerce.detail.ai.controller;

import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageJobControllerContractTest {

    @Test
    void imageJobControllerExposesRequiredRoutes() throws Exception {
        Class<?> controllerClass = Class.forName("com.ecommerce.detail.ai.controller.ImageJobController");
        assertControllerBasePath(controllerClass, "/image-jobs");
        assertRoute(controllerClass, "listImageJobs", RequestMethod.GET, "/list");
        assertRoute(controllerClass, "createImageJob", RequestMethod.POST, "");
        assertRoute(controllerClass, "getImageJobById", RequestMethod.GET, "/{id}");
        assertRoute(controllerClass, "updateImageJobStatus", RequestMethod.PUT, "/{id}/status");
        assertRoute(controllerClass, "retryImageJob", RequestMethod.POST, "/{id}/retry");
        assertRoute(controllerClass, "cancelImageJob", RequestMethod.POST, "/{id}/cancel");
    }

    private void assertControllerBasePath(Class<?> controllerClass, String expectedPath) {
        RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(controllerClass, RequestMapping.class);
        assertNotNull(mapping, controllerClass.getSimpleName() + " missing class-level request mapping");
        assertTrue(Arrays.asList(mapping.value()).contains(expectedPath),
                controllerClass.getSimpleName() + " should map to " + expectedPath);
        assertTrue(Arrays.stream(mapping.value()).noneMatch(path -> path != null && path.startsWith("/api")),
                controllerClass.getSimpleName() + " should not be prefixed with /api");
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
        assertTrue(Arrays.stream(mapping.value()).noneMatch(value -> value != null && value.startsWith("/api")),
                controllerClass.getSimpleName() + "." + methodName + " should not be prefixed with /api");
    }
}
