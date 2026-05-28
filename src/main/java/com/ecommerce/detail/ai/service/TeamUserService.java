package com.ecommerce.detail.ai.service;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.TeamUserCreateRequest;
import com.ecommerce.detail.ai.dto.TeamUserDTO;

public interface TeamUserService {
    PageResult<TeamUserDTO> listUsers(int pageNum, int pageSize, String keyword, String status);
    Long createUser(TeamUserCreateRequest request);
    TeamUserDTO getUserById(Long id);
    boolean updateUser(Long id, TeamUserDTO dto);
    boolean deleteUser(Long id);
}