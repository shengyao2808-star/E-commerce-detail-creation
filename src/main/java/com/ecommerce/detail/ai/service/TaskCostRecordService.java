package com.ecommerce.detail.ai.service;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.TaskCostRecordDTO;

import java.math.BigDecimal;
import java.util.List;

public interface TaskCostRecordService {

    PageResult<TaskCostRecordDTO> listCostRecords(int pageNum, int pageSize, String taskType, Long taskId,
                                                   String toolCode, String modelCode, Long visualPlanId);

    Long createCostRecord(TaskCostRecordDTO dto);

    TaskCostRecordDTO getCostRecordById(Long id);

    boolean updateCostRecord(Long id, TaskCostRecordDTO dto);

    BigDecimal sumCostByVisualPlanId(Long visualPlanId);
}
