package com.ecommerce.detail.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.ProductContentTaskApplyDTO;
import com.ecommerce.detail.ai.dto.ProductContentTaskDTO;
import com.ecommerce.detail.ai.dto.ProductContentTaskRequestDTO;
import com.ecommerce.detail.ai.entity.ProductContentTask;

public interface ProductContentTaskService extends IService<ProductContentTask> {

    PageResult<ProductContentTaskDTO> listProductContentTasks(int pageNum, int pageSize, Long productDetailId, String status);

    ProductContentTaskDTO getProductContentTaskById(Long id);

    ProductContentTaskDTO createProductContentTask(ProductContentTaskRequestDTO dto);

    ProductContentTaskDTO applyProductContentTask(Long id, ProductContentTaskApplyDTO dto);
}
