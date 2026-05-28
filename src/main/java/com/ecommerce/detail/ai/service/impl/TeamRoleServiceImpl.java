package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.TeamRoleDTO;
import com.ecommerce.detail.ai.entity.TeamRole;
import com.ecommerce.detail.ai.exception.ResourceNotFoundException;
import com.ecommerce.detail.ai.mapper.TeamRoleMapper;
import com.ecommerce.detail.ai.service.TeamRoleService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeamRoleServiceImpl extends ServiceImpl<TeamRoleMapper, TeamRole> implements TeamRoleService {

    @Override
    public PageResult<TeamRoleDTO> listRoles(int pageNum, int pageSize) {
        pageNum = Math.max(pageNum, 1);
        if (pageSize < 1 || pageSize > 100) pageSize = 20;
        LambdaQueryWrapper<TeamRole> w = new LambdaQueryWrapper<>();
        w.orderByDesc(TeamRole::getCreateTime);
        Page<TeamRole> page = this.page(new Page<>(pageNum, pageSize), w);
        return PageResult.success(page.getRecords().stream().map(this::toDTO).toList(), pageNum, pageSize, page.getTotal());
    }

    @Override
    public List<TeamRoleDTO> listAllRoles() {
        return this.list().stream().map(this::toDTO).toList();
    }

    @Override
    public Long createRole(TeamRoleDTO dto) {
        TeamRole r = new TeamRole();
        r.setRoleCode(dto.getRoleCode());
        r.setRoleName(dto.getRoleName());
        r.setDescription(dto.getDescription());
        r.setCreateTime(LocalDateTime.now());
        r.setUpdateTime(LocalDateTime.now());
        this.save(r);
        return r.getId();
    }

    @Override
    public TeamRoleDTO getRoleById(Long id) {
        TeamRole r = this.getById(id);
        if (r == null) throw new ResourceNotFoundException("Role not found: " + id);
        return toDTO(r);
    }

    @Override
    public boolean updateRole(Long id, TeamRoleDTO dto) {
        TeamRole r = this.getById(id);
        if (r == null) throw new ResourceNotFoundException("Role not found: " + id);
        if (StringUtils.hasText(dto.getRoleName())) r.setRoleName(dto.getRoleName());
        if (StringUtils.hasText(dto.getDescription())) r.setDescription(dto.getDescription());
        r.setUpdateTime(LocalDateTime.now());
        return this.updateById(r);
    }

    @Override
    public boolean deleteRole(Long id) {
        return this.removeById(id);
    }

    private TeamRoleDTO toDTO(TeamRole r) {
        TeamRoleDTO dto = new TeamRoleDTO();
        dto.setId(r.getId());
        dto.setRoleCode(r.getRoleCode());
        dto.setRoleName(r.getRoleName());
        dto.setDescription(r.getDescription());
        dto.setCreateTime(r.getCreateTime());
        dto.setUpdateTime(r.getUpdateTime());
        return dto;
    }
}