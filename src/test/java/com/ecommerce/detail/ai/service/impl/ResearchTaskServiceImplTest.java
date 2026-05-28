package com.ecommerce.detail.ai.service.impl;

import com.ecommerce.detail.ai.dto.ResearchTaskChartsDTO;
import com.ecommerce.detail.ai.dto.ResearchTaskDTO;
import com.ecommerce.detail.ai.dto.ResearchTaskResultDTO;
import com.ecommerce.detail.ai.entity.ResearchTask;
import com.ecommerce.detail.ai.mapper.ResearchTaskMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResearchTaskServiceImplTest {

    @Test
    void getResearchTaskChartsReturnsEmptyCollectionsWhenResultIsMissing() {
        ResearchTask task = new ResearchTask();
        task.setId(11L);
        task.setTaskName("空图表任务");
        task.setStatus("SUCCEEDED");
        task.setResultJson(null);

        RecordingResearchTaskMapper mapper = new RecordingResearchTaskMapper(task);
        ResearchTaskServiceImpl service = service(mapper);

        ResearchTaskChartsDTO charts = service.getResearchTaskCharts(11L);

        assertNotNull(charts);
        assertEquals(List.of(), charts.getPriceBands());
        assertEquals(List.of(), charts.getKeywordRanking());
        assertEquals(List.of(), charts.getPainPointRanking());
        assertEquals(List.of(), charts.getCompetitorMatrix());
        assertEquals(0, mapper.updateCount);
    }

    @Test
    void updateResearchTaskResultPersistsSerializedResultJson() {
        ResearchTask task = new ResearchTask();
        task.setId(12L);
        task.setTaskName("图表结果任务");
        task.setStatus("RUNNING");

        RecordingResearchTaskMapper mapper = new RecordingResearchTaskMapper(task);
        ResearchTaskServiceImpl service = service(mapper);

        ResearchTaskResultDTO dto = new ResearchTaskResultDTO();
        dto.setResultData(Map.of(
                "priceBands", List.of(Map.of("label", "0-50", "value", 12)),
                "keywordRanking", List.of(Map.of("label", "便携", "value", 8)),
                "painPointRanking", List.of(Map.of("label", "续航", "value", 5)),
                "competitorMatrix", List.of(Map.of("name", "竞品A", "x", 4, "y", 8))
        ));

        boolean updated = service.updateResearchTaskResult(12L, dto);

        assertTrue(updated);
        assertEquals(1, mapper.updateCount);
        assertEquals("SUCCEEDED", mapper.updatedTask.getStatus());
        assertTrue(mapper.updatedTask.getResultJson().contains("\"priceBands\""));
        assertTrue(mapper.updatedTask.getResultJson().contains("\"competitorMatrix\""));
    }

    @Test
    void createResearchTaskSerializesInputJson() {
        RecordingResearchTaskMapper mapper = new RecordingResearchTaskMapper(null);
        ResearchTaskServiceImpl service = service(mapper);

        ResearchTaskDTO dto = new ResearchTaskDTO();
        dto.setTaskName("创建调研任务");
        dto.setCategory("手机");
        dto.setOwner("alice");
        dto.setInputData(Map.of("keywords", List.of("续航", "拍照")));

        Long id = service.createResearchTask(dto);

        assertEquals(101L, id);
        assertEquals(1, mapper.insertCount);
        assertEquals("PENDING", mapper.insertedTask.getStatus());
        assertTrue(mapper.insertedTask.getInputJson().contains("\"keywords\""));
    }

    private static ResearchTaskServiceImpl service(RecordingResearchTaskMapper mapper) {
        ResearchTaskServiceImpl service = new ResearchTaskServiceImpl();
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

    private static class RecordingResearchTaskMapper {
        private final ResearchTask storedTask;
        private ResearchTask insertedTask;
        private ResearchTask updatedTask;
        private int insertCount;
        private int updateCount;

        private RecordingResearchTaskMapper(ResearchTask storedTask) {
            this.storedTask = storedTask;
        }

        private ResearchTaskMapper proxy() {
            return (ResearchTaskMapper) Proxy.newProxyInstance(
                    ResearchTaskMapper.class.getClassLoader(),
                    new Class<?>[]{ResearchTaskMapper.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();
                        if ("selectById".equals(methodName)) {
                            Serializable id = (Serializable) args[0];
                            return storedTask != null && storedTask.getId().equals(id) ? storedTask : null;
                        }
                        if ("insert".equals(methodName)) {
                            insertedTask = (ResearchTask) args[0];
                            insertedTask.setId(101L);
                            insertCount++;
                            return 1;
                        }
                        if ("updateById".equals(methodName)) {
                            updatedTask = (ResearchTask) args[0];
                            updateCount++;
                            return 1;
                        }
                        return defaultValue(method.getReturnType());
                    });
        }
    }
}
