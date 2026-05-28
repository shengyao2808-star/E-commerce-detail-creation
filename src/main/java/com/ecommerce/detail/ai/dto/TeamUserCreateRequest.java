package com.ecommerce.detail.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeamUserCreateRequest {
    @NotBlank(message = "username is required")
    private String username;
    @NotBlank(message = "displayName is required")
    private String displayName;
    private String email;
    private String avatarUrl;
}
