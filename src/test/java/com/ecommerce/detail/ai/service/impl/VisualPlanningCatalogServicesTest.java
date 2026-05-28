package com.ecommerce.detail.ai.service.impl;

import com.ecommerce.detail.ai.dto.CategoryVisualPolicyDTO;
import com.ecommerce.detail.ai.dto.ModelProfileDTO;
import com.ecommerce.detail.ai.dto.SkcPolicyDTO;
import com.ecommerce.detail.ai.entity.CategoryVisualPolicy;
import com.ecommerce.detail.ai.entity.ModelProfile;
import com.ecommerce.detail.ai.entity.SkcPolicy;
import com.ecommerce.detail.ai.mapper.CategoryVisualPolicyMapper;
import com.ecommerce.detail.ai.mapper.ModelProfileMapper;
import com.ecommerce.detail.ai.mapper.SkcPolicyMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VisualPlanningCatalogServicesTest {

    @Test
    void categoryVisualPolicyRoundTripsJsonAndStatus() {
        RecordingCategoryPolicyMapper mapper = new RecordingCategoryPolicyMapper(null);
        CategoryVisualPolicyServiceImpl service = categoryService(mapper);

        CategoryVisualPolicyDTO dto = new CategoryVisualPolicyDTO();
        dto.setCategoryCode("women-dress");
        dto.setCategoryName("women-dress-name");
        dto.setAllowedShotTypes(List.of("MODEL", "FLAT_LAY"));
        dto.setRequiredMainImages(Map.of("main", 5));
        dto.setDetailScreenCountRange(Map.of("min", 6, "max", 10));
        dto.setRiskRules(List.of("no fake claims"));

        Long id = service.createCategoryVisualPolicy(dto);
        assertEquals(301L, id);
        assertEquals("[\"MODEL\",\"FLAT_LAY\"]", mapper.inserted.getAllowedShotTypesJson());

        CategoryVisualPolicy stored = mapper.inserted;
        stored.setId(301L);
        stored.setStatus("CONFIRMED");
        mapper = new RecordingCategoryPolicyMapper(stored);
        service = categoryService(mapper);

        CategoryVisualPolicyDTO roundTrip = service.getCategoryVisualPolicyById(301L);
        assertEquals("women-dress", roundTrip.getCategoryCode());
        assertEquals(5, roundTrip.getRequiredMainImages().get("main"));
        assertEquals(2, roundTrip.getAllowedShotTypes().size());

        CategoryVisualPolicyDTO update = new CategoryVisualPolicyDTO();
        update.setStatus("ARCHIVED");
        update.setCategoryName("updated-name");
        assertEquals(true, service.updateCategoryVisualPolicy(301L, update));
        assertEquals("ARCHIVED", mapper.inserted.getStatus());
    }

    @Test
    void modelProfileRoundTripsJsonAndVersionBumps() {
        RecordingModelProfileMapper mapper = new RecordingModelProfileMapper(null);
        ModelProfileServiceImpl service = modelService(mapper);

        ModelProfileDTO dto = new ModelProfileDTO();
        dto.setDisplayName("model-a");
        dto.setHeight(new BigDecimal("175.5"));
        dto.setStyleTags(List.of("clean", "premium"));
        dto.setCategoryScopes(List.of("women-dress"));

        Long id = service.createModelProfile(dto);
        assertEquals(401L, id);
        assertEquals("[\"clean\",\"premium\"]", mapper.inserted.getStyleTagsJson());

        ModelProfile stored = mapper.inserted;
        stored.setId(401L);
        mapper = new RecordingModelProfileMapper(stored);
        service = modelService(mapper);

        ModelProfileDTO roundTrip = service.getModelProfileById(401L);
        assertEquals("model-a", roundTrip.getDisplayName());
        assertEquals(new BigDecimal("175.5"), roundTrip.getHeight());

        ModelProfileDTO update = new ModelProfileDTO();
        update.setStatus("CONFIRMED");
        assertEquals(true, service.updateModelProfile(401L, update));
        assertEquals("CONFIRMED", mapper.inserted.getStatus());
        assertEquals(2, mapper.inserted.getVersion());
    }

    @Test
    void skcPolicyRoundTripsJsonAndStatusValidationWorks() {
        RecordingSkcPolicyMapper mapper = new RecordingSkcPolicyMapper(null);
        SkcPolicyServiceImpl service = skcService(mapper);

        SkcPolicyDTO dto = new SkcPolicyDTO();
        dto.setPolicyName("SKC-A");
        dto.setColors(List.of(Map.of("color", "black")));
        dto.setSpecs(List.of(Map.of("size", "M")));
        dto.setGenerationRules(List.of("keep one model"));

        Long id = service.createSkcPolicy(dto);
        assertEquals(501L, id);
        assertEquals("[{\"color\":\"black\"}]", mapper.inserted.getColorsJson());

        SkcPolicy stored = mapper.inserted;
        stored.setId(501L);
        mapper = new RecordingSkcPolicyMapper(stored);
        service = skcService(mapper);

        SkcPolicyDTO roundTrip = service.getSkcPolicyById(501L);
        assertEquals("SKC-A", roundTrip.getPolicyName());
        assertEquals("black", ((Map<?, ?>) roundTrip.getColors().get(0)).get("color"));

        SkcPolicyDTO update = new SkcPolicyDTO();
        update.setStatus("ARCHIVED");
        assertEquals(true, service.updateSkcPolicy(501L, update));
        assertEquals("ARCHIVED", mapper.inserted.getStatus());
    }

    @Test
    void categoryPolicyRejectsUnsupportedStatus() {
        CategoryVisualPolicyServiceImpl service = categoryService(new RecordingCategoryPolicyMapper(null));
        CategoryVisualPolicyDTO dto = new CategoryVisualPolicyDTO();
        dto.setCategoryCode("invalid");
        dto.setStatus("completed");
        assertThrows(IllegalArgumentException.class, () -> service.createCategoryVisualPolicy(dto));
    }

    @Test
    void categoryVisualPolicyConfirmTransitionsDraftToConfirmed() {
        RecordingCategoryPolicyMapper mapper = new RecordingCategoryPolicyMapper(null);
        CategoryVisualPolicyServiceImpl service = categoryService(mapper);

        CategoryVisualPolicyDTO dto = new CategoryVisualPolicyDTO();
        dto.setCategoryCode("women-shoes");
        dto.setCategoryName("women-shoes-name");
        service.createCategoryVisualPolicy(dto);
        assertEquals("DRAFT", mapper.inserted.getStatus());

        mapper.inserted.setId(301L);
        mapper = new RecordingCategoryPolicyMapper(mapper.inserted);
        service = categoryService(mapper);
        CategoryVisualPolicyDTO confirmed = service.confirmCategoryVisualPolicy(301L);
        assertEquals("CONFIRMED", confirmed.getStatus());
        assertEquals(2, confirmed.getVersion());
    }

    @Test
    void modelProfileConfirmTransitionsDraftToConfirmed() {
        RecordingModelProfileMapper mapper = new RecordingModelProfileMapper(null);
        ModelProfileServiceImpl service = modelService(mapper);

        ModelProfileDTO dto = new ModelProfileDTO();
        dto.setDisplayName("model-b");
        service.createModelProfile(dto);
        assertEquals("DRAFT", mapper.inserted.getStatus());

        mapper.inserted.setId(401L);
        mapper = new RecordingModelProfileMapper(mapper.inserted);
        service = modelService(mapper);
        ModelProfileDTO confirmed = service.confirmModelProfile(401L);
        assertEquals("CONFIRMED", confirmed.getStatus());
        assertEquals(2, confirmed.getVersion());
    }

    @Test
    void skcPolicyConfirmTransitionsDraftToConfirmed() {
        RecordingSkcPolicyMapper mapper = new RecordingSkcPolicyMapper(null);
        SkcPolicyServiceImpl service = skcService(mapper);

        SkcPolicyDTO dto = new SkcPolicyDTO();
        dto.setPolicyName("SKC-CONFIRM-TEST");
        service.createSkcPolicy(dto);
        assertEquals("DRAFT", mapper.inserted.getStatus());

        mapper.inserted.setId(501L);
        mapper = new RecordingSkcPolicyMapper(mapper.inserted);
        service = skcService(mapper);
        SkcPolicyDTO confirmed = service.confirmSkcPolicy(501L);
        assertEquals("CONFIRMED", confirmed.getStatus());
        assertEquals(2, confirmed.getVersion());
    }

    private CategoryVisualPolicyServiceImpl categoryService(RecordingCategoryPolicyMapper mapper) {
        CategoryVisualPolicyServiceImpl service = new CategoryVisualPolicyServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper.proxy());
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        return service;
    }

    private ModelProfileServiceImpl modelService(RecordingModelProfileMapper mapper) {
        ModelProfileServiceImpl service = new ModelProfileServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper.proxy());
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        return service;
    }

    private SkcPolicyServiceImpl skcService(RecordingSkcPolicyMapper mapper) {
        SkcPolicyServiceImpl service = new SkcPolicyServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper.proxy());
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        return service;
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

    private static class RecordingCategoryPolicyMapper {
        private final CategoryVisualPolicy stored;
        private CategoryVisualPolicy inserted;

        private RecordingCategoryPolicyMapper(CategoryVisualPolicy stored) {
            this.stored = stored;
        }

        private CategoryVisualPolicyMapper proxy() {
            return (CategoryVisualPolicyMapper) Proxy.newProxyInstance(
                    CategoryVisualPolicyMapper.class.getClassLoader(),
                    new Class<?>[]{CategoryVisualPolicyMapper.class},
                    (proxy, method, args) -> {
                        if ("selectById".equals(method.getName())) {
                            Serializable id = (Serializable) args[0];
                            return stored != null && stored.getId().equals(id) ? stored : null;
                        }
                        if ("insert".equals(method.getName())) {
                            inserted = (CategoryVisualPolicy) args[0];
                            inserted.setId(301L);
                            return 1;
                        }
                        if ("updateById".equals(method.getName())) {
                            inserted = (CategoryVisualPolicy) args[0];
                            return 1;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }

    private static class RecordingModelProfileMapper {
        private final ModelProfile stored;
        private ModelProfile inserted;

        private RecordingModelProfileMapper(ModelProfile stored) {
            this.stored = stored;
        }

        private ModelProfileMapper proxy() {
            return (ModelProfileMapper) Proxy.newProxyInstance(
                    ModelProfileMapper.class.getClassLoader(),
                    new Class<?>[]{ModelProfileMapper.class},
                    (proxy, method, args) -> {
                        if ("selectById".equals(method.getName())) {
                            Serializable id = (Serializable) args[0];
                            return stored != null && stored.getId().equals(id) ? stored : null;
                        }
                        if ("insert".equals(method.getName())) {
                            inserted = (ModelProfile) args[0];
                            inserted.setId(401L);
                            return 1;
                        }
                        if ("updateById".equals(method.getName())) {
                            inserted = (ModelProfile) args[0];
                            return 1;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }

    private static class RecordingSkcPolicyMapper {
        private final SkcPolicy stored;
        private SkcPolicy inserted;

        private RecordingSkcPolicyMapper(SkcPolicy stored) {
            this.stored = stored;
        }

        private SkcPolicyMapper proxy() {
            return (SkcPolicyMapper) Proxy.newProxyInstance(
                    SkcPolicyMapper.class.getClassLoader(),
                    new Class<?>[]{SkcPolicyMapper.class},
                    (proxy, method, args) -> {
                        if ("selectById".equals(method.getName())) {
                            Serializable id = (Serializable) args[0];
                            return stored != null && stored.getId().equals(id) ? stored : null;
                        }
                        if ("insert".equals(method.getName())) {
                            inserted = (SkcPolicy) args[0];
                            inserted.setId(501L);
                            return 1;
                        }
                        if ("updateById".equals(method.getName())) {
                            inserted = (SkcPolicy) args[0];
                            return 1;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }
}