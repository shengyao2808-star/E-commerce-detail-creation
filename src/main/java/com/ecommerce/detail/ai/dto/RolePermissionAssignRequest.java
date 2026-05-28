package com.ecommerce.detail.ai.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class RolePermissionAssignRequest {
    @NotNull(message = "roleId is required")
    private Long roleId;
    @NotEmpty(message = "permissionCodes is required")
    private List<String> permissionCodes;
}
