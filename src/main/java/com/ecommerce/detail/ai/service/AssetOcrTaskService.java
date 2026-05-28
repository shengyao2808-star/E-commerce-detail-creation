package com.ecommerce.detail.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.AssetOcrTaskDTO;
import com.ecommerce.detail.ai.dto.AssetOcrTaskResultDTO;
import com.ecommerce.detail.ai.dto.AssetOcrTaskStatusDTO;
import com.ecommerce.detail.ai.entity.AssetOcrTask;

public interface AssetOcrTaskService extends IService<AssetOcrTask> {

    Long createAssetOcrTask(AssetOcrTaskDTO dto);

    AssetOcrTaskDTO getAssetOcrTaskById(Long id);

    PageResult<AssetOcrTaskDTO> listAssetOcrTasks(int pageNum, int pageSize, Long materialId, String status, String language);

    boolean updateAssetOcrTaskStatus(Long id, AssetOcrTaskStatusDTO dto);

    boolean updateAssetOcrTaskResult(Long id, AssetOcrTaskResultDTO dto);
}
