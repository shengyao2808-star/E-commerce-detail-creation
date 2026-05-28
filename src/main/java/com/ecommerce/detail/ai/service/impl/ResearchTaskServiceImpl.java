package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.ResearchTaskChartsDTO;
import com.ecommerce.detail.ai.dto.ResearchTaskDTO;
import com.ecommerce.detail.ai.dto.ResearchTaskResultDTO;
import com.ecommerce.detail.ai.entity.ResearchTask;
import com.ecommerce.detail.ai.mapper.ResearchTaskMapper;
import com.ecommerce.detail.ai.service.ResearchTaskService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ResearchTaskServiceImpl extends ServiceImpl<ResearchTaskMapper, ResearchTask> implements ResearchTaskService {

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "PENDING", "RUNNING", "SUCCEEDED", "FAILED", "CANCELED"
    );

    @Autowired(required = false)
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PageResult<ResearchTaskDTO> listResearchTasks(int pageNum, int pageSize, String keyword, String status) {
        pageNum = Math.max(pageNum, 1);
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        LambdaQueryWrapper<ResearchTask> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(q -> q.like(ResearchTask::getTaskName, keyword)
                    .or()
                    .like(ResearchTask::getCategory, keyword)
                    .or()
                    .like(ResearchTask::getOwner, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(ResearchTask::getStatus, normalizeStatus(status));
        }
        wrapper.orderByDesc(ResearchTask::getCreateTime);

        Page<ResearchTask> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<ResearchTaskDTO> records = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.success(records, pageNum, pageSize, page.getTotal());
    }

    @Override
    public Long createResearchTask(ResearchTaskDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getTaskName())) {
            throw new IllegalArgumentException("taskName must not be blank");
        }

        ResearchTask task = new ResearchTask();
        task.setTaskName(dto.getTaskName());
        task.setCategory(dto.getCategory());
        task.setOwner(dto.getOwner());
        task.setStatus("PENDING");
        task.setInputJson(writeJson(dto.getInputData()));
        task.setCreateTime(now());
        task.setUpdateTime(now());

        this.save(task);
        return task.getId();
    }

    @Override
    public ResearchTaskDTO getResearchTaskById(Long id) {
        ResearchTask task = requireTask(id);
        return toDTO(task);
    }

    @Override
    public boolean updateResearchTaskStatus(Long id, String status) {
        ResearchTask task = requireTask(id);
        task.setStatus(normalizeStatus(status));
        task.setUpdateTime(now());
        return this.updateById(task);
    }

    @Override
    public boolean updateResearchTaskResult(Long id, ResearchTaskResultDTO dto) {
        ResearchTask task = requireTask(id);
        Map<String, Object> resultData = dto == null ? Map.of() : dto.getResultData();
        task.setResultJson(writeJson(resultData));
        task.setStatus("SUCCEEDED");
        task.setUpdateTime(now());
        return this.updateById(task);
    }

    @Override
    public ResearchTaskChartsDTO getResearchTaskCharts(Long id) {
        ResearchTask task = requireTask(id);
        ResearchTaskChartsDTO dto = new ResearchTaskChartsDTO();
        if (!StringUtils.hasText(task.getResultJson())) {
            return dto;
        }

        Map<String, Object> resultData = readMap(task.getResultJson());
        dto.setPriceBands(readChartList(resultData.get("priceBands")));
        dto.setKeywordRanking(readChartList(resultData.get("keywordRanking")));
        dto.setPainPointRanking(readChartList(resultData.get("painPointRanking")));
        dto.setCompetitorMatrix(readChartList(resultData.get("competitorMatrix")));
        return dto;
    }

    private ResearchTask requireTask(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        ResearchTask task = this.getById(id);
        if (task == null) {
            throw new IllegalStateException("research task not found: " + id);
        }
        return task;
    }

    private ResearchTaskDTO toDTO(ResearchTask task) {
        ResearchTaskDTO dto = new ResearchTaskDTO();
        dto.setId(task.getId());
        dto.setTaskName(task.getTaskName());
        dto.setCategory(task.getCategory());
        dto.setOwner(task.getOwner());
        dto.setStatus(task.getStatus());
        dto.setInputData(readMap(task.getInputJson()));
        dto.setResultData(readMap(task.getResultJson()));
        dto.setCreateTime(task.getCreateTime());
        dto.setUpdateTime(task.getUpdateTime());
        return dto;
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("status must not be blank");
        }
        String normalized = status.trim().toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("unsupported research task status: " + status);
        }
        return normalized;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (Exception e) {
            throw new IllegalStateException("failed to serialize research task json", e);
        }
    }

    private Map<String, Object> readMap(String json) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("failed to parse research task json", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> readChartList(Object value) {
        if (value == null) {
            return List.of();
        }
        try {
            return objectMapper.convertValue(value, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now();
    }
}
