package com.ecommerce.detail.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.DetailCompositionCreateDTO;
import com.ecommerce.detail.ai.dto.DetailCompositionDTO;
import com.ecommerce.detail.ai.dto.DetailCompositionListQuery;
import com.ecommerce.detail.ai.dto.DetailCompositionQualityCheckDTO;
import com.ecommerce.detail.ai.dto.DetailDeliveryManifestDTO;
import com.ecommerce.detail.ai.entity.DetailComposition;

import java.io.File;

public interface DetailCompositionService extends IService<DetailComposition> {

    Long createDetailComposition(DetailCompositionCreateDTO dto);

    DetailCompositionDTO getDetailCompositionById(Long id);

    PageResult<DetailCompositionDTO> listDetailCompositions(DetailCompositionListQuery query);

    File resolveDownloadFile(Long id);

    Long createQualityCheck(Long id);

    PageResult<DetailCompositionQualityCheckDTO> listQualityChecks(Long id, Integer pageNum, Integer pageSize);

    DetailDeliveryManifestDTO getDeliveryManifest(Long id);
}
