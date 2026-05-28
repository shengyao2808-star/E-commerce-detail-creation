package com.ecommerce.detail.ai.service;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.PostProcessTaskCreateDTO;
import com.ecommerce.detail.ai.dto.PostProcessTaskDTO;
import com.ecommerce.detail.ai.dto.PostProcessTaskListQuery;

public interface PostProcessTaskService {

    PageResult<PostProcessTaskDTO> listPostProcessTasks(PostProcessTaskListQuery query);

    Long createPostProcessTask(PostProcessTaskCreateDTO dto);

    PostProcessTaskDTO getPostProcessTaskById(Long id);

    boolean retryPostProcessTask(Long id);

    boolean cancelPostProcessTask(Long id);

    boolean updateTaskStatus(Long id, String status, Integer progress, String errorMessage);
}
