package com.ecommerce.detail.ai.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class UserRoleAssignRequest {
    @NotNull(message = "userId is required")
    private Long userId;
    @NotEmpty(message = "roleCodes is required")
    private List<String> roleCodes;
}
