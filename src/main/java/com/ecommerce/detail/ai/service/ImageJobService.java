package com.ecommerce.detail.ai.service;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.ImageJobCreateDTO;
import com.ecommerce.detail.ai.dto.ImageJobDTO;
import com.ecommerce.detail.ai.dto.ImageJobRetryDTO;
import com.ecommerce.detail.ai.dto.ImageJobStatusDTO;

import java.util.List;

public interface ImageJobService {

    PageResult<ImageJobDTO> listImageJobs(int pageNum, int pageSize, String keyword, String status, String toolCode, Long visualPlanId);

    Long createImageJob(ImageJobCreateDTO dto);

    ImageJobDTO getImageJobById(Long id);

    boolean updateImageJobStatus(Long id, ImageJobStatusDTO dto);

    boolean retryImageJob(Long id, ImageJobRetryDTO dto);

    boolean cancelImageJob(Long id, ImageJobStatusDTO dto);

    List<ImageJobDTO> listByVisualPlanId(Long visualPlanId);
}
