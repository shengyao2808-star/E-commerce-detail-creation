package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.PromptTemplateCreateDTO;
import com.ecommerce.detail.ai.dto.PromptTemplateDTO;
import com.ecommerce.detail.ai.entity.PromptTemplate;
import com.ecommerce.detail.ai.mapper.PromptTemplateMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromptTemplateServiceImplTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void createTemplateSavesEntity() {
        RecordingMapper mapper = new RecordingMapper(null);
        PromptTemplateServiceImpl service = service(mapper);

        PromptTemplateCreateDTO dto = new PromptTemplateCreateDTO();
        dto.setTemplateName("Product Main Shot");
        dto.setCategory("PRODUCT_MAIN");
        dto.setPlatform("TAOBAO");
        dto.setPositivePrompt("high quality product photo, studio lighting");
        dto.setNegativePrompt("blurry, low quality");
        dto.setStyleTags(List.of("minimalist", "bright"));
        dto.setSource("CUSTOM");

        PromptTemplateDTO result = service.createTemplate(dto);

        assertNotNull(result);
        assertEquals("Product Main Shot", result.getTemplateName());
        assertEquals("PRODUCT_MAIN", result.getCategory());
        assertEquals(1, mapper.insertCount);
        assertEquals("ACTIVE", mapper.insertedEntity.getStatus());
    }

    @Test
    void listTemplatesAppliesFilters() {
        RecordingMapper mapper = new RecordingMapper(null);
        PromptTemplateServiceImpl service = service(mapper);

        PageResult<PromptTemplateDTO> result = service.listTemplates(1, 20, "PRODUCT_MAIN", "TAOBAO", null, null, null);

        assertNotNull(result);
        assertEquals(1, mapper.selectPageCount);
    }

    @Test
    void getTemplateByIdThrowsWhenNotFound() {
        RecordingMapper mapper = new RecordingMapper(null);
        PromptTemplateServiceImpl service = service(mapper);

        assertThrows(IllegalArgumentException.class, () -> service.getTemplateById(999L));
    }

    @Test
    void duplicateTemplateCreatesCopy() {
        PromptTemplate original = new PromptTemplate();
        original.setId(1L);
        original.setTemplateName("Original");
        original.setCategory("DETAIL_SCENE");
        original.setPositivePrompt("test prompt");
        original.setUsageCount(42);
        original.setRating(BigDecimal.valueOf(4.5));

        RecordingMapper mapper = new RecordingMapper(original);
        PromptTemplateServiceImpl service = service(mapper);

        PromptTemplateDTO result = service.duplicateTemplate(1L);

        assertEquals("Original (copy)", result.getTemplateName());
        assertEquals(0, result.getUsageCount().intValue());
        assertEquals("CUSTOM", result.getSource());
        assertEquals(1, mapper.insertCount);
    }

    @Test
    void incrementUsageCountIncreasesByOne() {
        PromptTemplate entity = new PromptTemplate();
        entity.setId(1L);
        entity.setTemplateName("Test");
        entity.setUsageCount(5);

        RecordingMapper mapper = new RecordingMapper(entity);
        PromptTemplateServiceImpl service = service(mapper);

        service.incrementUsageCount(1L);

        assertEquals(1, mapper.updateByIdCount);
        assertEquals(6, mapper.updatedEntity.getUsageCount());
    }

    @Test
    void deleteTemplateCallsRemoveById() {
        PromptTemplate entity = new PromptTemplate();
        entity.setId(1L);

        RecordingMapper mapper = new RecordingMapper(entity);
        PromptTemplateServiceImpl service = service(mapper);

        service.deleteTemplate(1L);

        assertTrue(mapper.deleteByIdCount > 0);
    }

    @Test
    void updateTemplateModifiesFields() {
        PromptTemplate entity = new PromptTemplate();
        entity.setId(1L);
        entity.setTemplateName("Old Name");
        entity.setCategory("PRODUCT_MAIN");
        entity.setPositivePrompt("old prompt");

        RecordingMapper mapper = new RecordingMapper(entity);
        PromptTemplateServiceImpl service = service(mapper);

        PromptTemplateCreateDTO dto = new PromptTemplateCreateDTO();
        dto.setTemplateName("New Name");
        dto.setPositivePrompt("new prompt");

        PromptTemplateDTO result = service.updateTemplate(1L, dto);

        assertEquals("New Name", result.getTemplateName());
        assertEquals("new prompt", result.getPositivePrompt());
        assertEquals(1, mapper.updateByIdCount);
    }

    // -- Helpers --

    private static PromptTemplateServiceImpl service(RecordingMapper mapper) {
        initTableInfo();
        PromptTemplateServiceImpl svc = new PromptTemplateServiceImpl();
        ReflectionTestUtils.setField(svc, "baseMapper", mapper.proxy());
        ReflectionTestUtils.setField(svc, "mapperClass", PromptTemplateMapper.class);
        ReflectionTestUtils.setField(svc, "objectMapper", MAPPER);
        return svc;
    }

    private static void initTableInfo() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(
                new org.apache.ibatis.session.Configuration(), "");
        assistant.setCurrentNamespace("com.ecommerce.detail.ai.mapper.PromptTemplateMapper");
        TableInfoHelper.initTableInfo(assistant, PromptTemplate.class);
    }

    private static class RecordingMapper {
        private final PromptTemplate storedEntity;
        PromptTemplate insertedEntity;
        PromptTemplate updatedEntity;
        int insertCount;
        int updateByIdCount;
        int selectPageCount;
        int deleteByIdCount;

        RecordingMapper(PromptTemplate storedEntity) {
            this.storedEntity = storedEntity;
        }

        PromptTemplateMapper proxy() {
            return (PromptTemplateMapper) Proxy.newProxyInstance(
                    PromptTemplateMapper.class.getClassLoader(),
                    new Class<?>[]{PromptTemplateMapper.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "selectById" -> {
                            Serializable id = (Serializable) args[0];
                            yield storedEntity != null && storedEntity.getId().equals(id) ? storedEntity : null;
                        }
                        case "insert" -> {
                            insertedEntity = (PromptTemplate) args[0];
                            insertedEntity.setId(100L);
                            insertCount++;
                            yield 1;
                        }
                        case "updateById" -> {
                            updatedEntity = (PromptTemplate) args[0];
                            updateByIdCount++;
                            yield 1;
                        }
                        case "selectPage" -> {
                            selectPageCount++;
                            @SuppressWarnings("unchecked")
                            IPage<PromptTemplate> page = (IPage<PromptTemplate>) args[0];
                            page.setRecords(storedEntity != null ? List.of(storedEntity) : List.of());
                            page.setTotal(storedEntity != null ? 1 : 0);
                            yield page;
                        }
                        case "deleteById" -> {
                            deleteByIdCount++;
                            yield 1;
                        }
                        default -> 0;
                    });
        }
    }
}