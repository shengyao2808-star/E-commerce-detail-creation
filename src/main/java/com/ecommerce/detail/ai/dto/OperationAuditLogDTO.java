package com.ecommerce.detail.ai.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OperationAuditLogDTO {
    private Long id;
    private Long operatorId;
    private String operatorName;
    private String action;
    private String targetType;
    private Long targetId;
    private String detailJson;
    private LocalDateTime createTime;
}
