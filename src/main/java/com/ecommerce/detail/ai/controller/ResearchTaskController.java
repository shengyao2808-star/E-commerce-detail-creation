package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.ResearchTaskChartsDTO;
import com.ecommerce.detail.ai.dto.ResearchTaskDTO;
import com.ecommerce.detail.ai.dto.ResearchTaskResultDTO;
import com.ecommerce.detail.ai.dto.ResearchTaskStatusDTO;
import com.ecommerce.detail.ai.service.ResearchTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/research/tasks")
public class ResearchTaskController {

    @Autowired
    private ResearchTaskService researchTaskService;

    @GetMapping("/list")
    public Result<PageResult<ResearchTaskDTO>> listResearchTasks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.success(researchTaskService.listResearchTasks(pageNum, pageSize, keyword, status));
    }

    @PostMapping("")
    public Result<Long> createResearchTask(@RequestBody ResearchTaskDTO dto) {
        return Result.success(researchTaskService.createResearchTask(dto));
    }

    @GetMapping("/{id}")
    public Result<ResearchTaskDTO> getResearchTaskById(@PathVariable Long id) {
        return Result.success(researchTaskService.getResearchTaskById(id));
    }

    @PutMapping("/{id}/status")
    public Result<Boolean> updateResearchTaskStatus(@PathVariable Long id, @RequestBody(required = false) ResearchTaskStatusDTO dto) {
        return Result.success(researchTaskService.updateResearchTaskStatus(id, dto == null ? null : dto.getStatus()));
    }

    @PutMapping("/{id}/result")
    public Result<Boolean> updateResearchTaskResult(@PathVariable Long id, @RequestBody(required = false) ResearchTaskResultDTO dto) {
        return Result.success(researchTaskService.updateResearchTaskResult(id, dto));
    }

    @GetMapping("/{id}/charts")
    public Result<ResearchTaskChartsDTO> getResearchTaskCharts(@PathVariable Long id) {
        return Result.success(researchTaskService.getResearchTaskCharts(id));
    }
}
