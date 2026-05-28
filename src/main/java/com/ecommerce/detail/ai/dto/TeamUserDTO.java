package com.ecommerce.detail.ai.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TeamUserDTO {
    private Long id;
    private String username;
    private String displayName;
    private String email;
    private String avatarUrl;
    private String status;
    private List<String> roles;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
