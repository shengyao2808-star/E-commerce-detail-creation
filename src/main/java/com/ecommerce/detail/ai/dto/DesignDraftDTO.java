package com.ecommerce.detail.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class DesignDraftDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long productDetailId;
    private Long productMaterialId;
    private String draftName;
    private String sceneJson;
    private List<Map<String, Object>> selectedAssets;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
