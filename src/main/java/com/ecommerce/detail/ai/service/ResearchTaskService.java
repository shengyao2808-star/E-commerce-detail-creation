package com.ecommerce.detail.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.ResearchTaskChartsDTO;
import com.ecommerce.detail.ai.dto.ResearchTaskDTO;
import com.ecommerce.detail.ai.dto.ResearchTaskResultDTO;
import com.ecommerce.detail.ai.entity.ResearchTask;

public interface ResearchTaskService extends IService<ResearchTask> {

    PageResult<ResearchTaskDTO> listResearchTasks(int pageNum, int pageSize, String keyword, String status);

    Long createResearchTask(ResearchTaskDTO dto);

    ResearchTaskDTO getResearchTaskById(Long id);

    boolean updateResearchTaskStatus(Long id, String status);

    boolean updateResearchTaskResult(Long id, ResearchTaskResultDTO dto);

    ResearchTaskChartsDTO getResearchTaskCharts(Long id);
}
