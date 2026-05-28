package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.AssetOcrTaskDTO;
import com.ecommerce.detail.ai.dto.AssetOcrTaskResultDTO;
import com.ecommerce.detail.ai.dto.AssetOcrTaskStatusDTO;
import com.ecommerce.detail.ai.service.AssetOcrTaskService;
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
@RequestMapping("/assets/ocr-tasks")
public class AssetOcrTaskController {

    @Autowired
    private AssetOcrTaskService assetOcrTaskService;

    @GetMapping("/list")
    public Result<PageResult<AssetOcrTaskDTO>> listAssetOcrTasks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long materialId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String language) {
        return Result.success(assetOcrTaskService.listAssetOcrTasks(pageNum, pageSize, materialId, status, language));
    }

    @PostMapping("")
    public Result<Long> createAssetOcrTask(@RequestBody AssetOcrTaskDTO dto) {
        return Result.success(assetOcrTaskService.createAssetOcrTask(dto));
    }

    @GetMapping("/{id}")
    public Result<AssetOcrTaskDTO> getAssetOcrTaskById(@PathVariable Long id) {
        return Result.success(assetOcrTaskService.getAssetOcrTaskById(id));
    }

    @PutMapping("/{id}/status")
    public Result<Boolean> updateAssetOcrTaskStatus(@PathVariable Long id, @RequestBody AssetOcrTaskStatusDTO dto) {
        return Result.success(assetOcrTaskService.updateAssetOcrTaskStatus(id, dto));
    }

    @PutMapping("/{id}/result")
    public Result<Boolean> updateAssetOcrTaskResult(@PathVariable Long id, @RequestBody AssetOcrTaskResultDTO dto) {
        return Result.success(assetOcrTaskService.updateAssetOcrTaskResult(id, dto));
    }
}
