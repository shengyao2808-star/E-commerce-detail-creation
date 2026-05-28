package com.ecommerce.detail.ai.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeamPermissionDTO {
    private Long id;
    private String permissionCode;
    private String permissionName;
    private String description;
    private LocalDateTime createTime;
}
