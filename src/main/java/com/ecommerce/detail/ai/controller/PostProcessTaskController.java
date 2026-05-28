package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.PostProcessTaskCreateDTO;
import com.ecommerce.detail.ai.dto.PostProcessTaskDTO;
import com.ecommerce.detail.ai.dto.PostProcessTaskListQuery;
import com.ecommerce.detail.ai.service.PostProcessTaskService;
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
@RequestMapping("/post-process-tasks")
public class PostProcessTaskController {

    @Autowired
    private PostProcessTaskService postProcessTaskService;

    @GetMapping({"", "/list"})
    public Result<PageResult<PostProcessTaskDTO>> listPostProcessTasks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long sourceGenerationResultId,
            @RequestParam(required = false) String toolCode,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String status) {
        PostProcessTaskListQuery query = new PostProcessTaskListQuery();
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        query.setSourceGenerationResultId(sourceGenerationResultId);
        query.setToolCode(toolCode);
        query.setOperation(operation);
        query.setStatus(status);
        return Result.success(postProcessTaskService.listPostProcessTasks(query));
    }

    @PostMapping
    public Result<Long> createPostProcessTask(@RequestBody PostProcessTaskCreateDTO dto) {
        return Result.success(postProcessTaskService.createPostProcessTask(dto));
    }

    @GetMapping("/{id}")
    public Result<PostProcessTaskDTO> getPostProcessTaskById(@PathVariable Long id) {
        return Result.success(postProcessTaskService.getPostProcessTaskById(id));
    }

    @PostMapping("/{id}/retry")
    public Result<Boolean> retryPostProcessTask(@PathVariable Long id) {
        return Result.success(postProcessTaskService.retryPostProcessTask(id));
    }

    @PostMapping("/{id}/cancel")
    public Result<Boolean> cancelPostProcessTask(@PathVariable Long id) {
        return Result.success(postProcessTaskService.cancelPostProcessTask(id));
    }
}
