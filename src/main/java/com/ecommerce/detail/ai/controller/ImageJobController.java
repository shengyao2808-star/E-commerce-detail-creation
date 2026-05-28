package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.ImageJobCreateDTO;
import com.ecommerce.detail.ai.dto.ImageJobDTO;
import com.ecommerce.detail.ai.dto.ImageJobRetryDTO;
import com.ecommerce.detail.ai.dto.ImageJobStatusDTO;
import com.ecommerce.detail.ai.service.ImageJobService;
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
@RequestMapping("/image-jobs")
public class ImageJobController {

    @Autowired
    private ImageJobService imageJobService;

    @GetMapping("/list")
    public Result<PageResult<ImageJobDTO>> listImageJobs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String toolCode,
            @RequestParam(required = false) Long visualPlanId) {
        return Result.success(imageJobService.listImageJobs(pageNum, pageSize, keyword, status, toolCode, visualPlanId));
    }

    @PostMapping("")
    public Result<Long> createImageJob(@RequestBody ImageJobCreateDTO dto) {
        return Result.success(imageJobService.createImageJob(dto));
    }

    @GetMapping("/{id}")
    public Result<ImageJobDTO> getImageJobById(@PathVariable Long id) {
        return Result.success(imageJobService.getImageJobById(id));
    }

    @PutMapping("/{id}/status")
    public Result<Boolean> updateImageJobStatus(@PathVariable Long id, @RequestBody ImageJobStatusDTO dto) {
        return Result.success(imageJobService.updateImageJobStatus(id, dto));
    }

    @PostMapping("/{id}/retry")
    public Result<Boolean> retryImageJob(@PathVariable Long id, @RequestBody(required = false) ImageJobRetryDTO dto) {
        return Result.success(imageJobService.retryImageJob(id, dto));
    }

    @PostMapping("/{id}/cancel")
    public Result<Boolean> cancelImageJob(@PathVariable Long id, @RequestBody(required = false) ImageJobStatusDTO dto) {
        return Result.success(imageJobService.cancelImageJob(id, dto));
    }
}
