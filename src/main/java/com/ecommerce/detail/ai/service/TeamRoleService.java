package com.ecommerce.detail.ai.service;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.TeamRoleDTO;
import java.util.List;

public interface TeamRoleService {
    PageResult<TeamRoleDTO> listRoles(int pageNum, int pageSize);
    List<TeamRoleDTO> listAllRoles();
    Long createRole(TeamRoleDTO dto);
    TeamRoleDTO getRoleById(Long id);
    boolean updateRole(Long id, TeamRoleDTO dto);
    boolean deleteRole(Long id);
}