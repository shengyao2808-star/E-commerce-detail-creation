package com.ecommerce.detail.ai.dto;

import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
public class CurrentUserDTO {
    private Long id;
    private String username;
    private String displayName;
    private String email;
    private String avatarUrl;
    private Set<String> roles;
    private Set<String> permissions;
}
