package com.ecommerce.detail.ai.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TeamRoleDTO {
    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
    private List<String> permissions;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
