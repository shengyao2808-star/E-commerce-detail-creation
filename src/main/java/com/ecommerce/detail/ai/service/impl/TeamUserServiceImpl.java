package com.ecommerce.detail.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.dto.TeamUserCreateRequest;
import com.ecommerce.detail.ai.dto.TeamUserDTO;
import com.ecommerce.detail.ai.entity.TeamUser;
import com.ecommerce.detail.ai.exception.ResourceNotFoundException;
import com.ecommerce.detail.ai.mapper.TeamUserMapper;
import com.ecommerce.detail.ai.service.TeamUserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeamUserServiceImpl extends ServiceImpl<TeamUserMapper, TeamUser> implements TeamUserService {

    @Override
    public PageResult<TeamUserDTO> listUsers(int pageNum, int pageSize, String keyword, String status) {
        pageNum = Math.max(pageNum, 1);
        if (pageSize < 1 || pageSize > 100) pageSize = 20;
        LambdaQueryWrapper<TeamUser> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(q -> q.like(TeamUser::getUsername, keyword).or().like(TeamUser::getDisplayName, keyword).or().like(TeamUser::getEmail, keyword));
        }
        if (StringUtils.hasText(status)) w.eq(TeamUser::getStatus, status.trim());
        w.orderByDesc(TeamUser::getCreateTime);
        Page<TeamUser> page = this.page(new Page<>(pageNum, pageSize), w);
        return PageResult.success(page.getRecords().stream().map(this::toDTO).toList(), pageNum, pageSize, page.getTotal());
    }

    @Override
    public Long createUser(TeamUserCreateRequest req) {
        TeamUser user = new TeamUser();
        user.setUsername(req.getUsername().trim());
        user.setDisplayName(req.getDisplayName().trim());
        user.setEmail(req.getEmail());
        user.setAvatarUrl(req.getAvatarUrl());
        user.setStatus("ACTIVE");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        this.save(user);
        return user.getId();
    }

    @Override
    public TeamUserDTO getUserById(Long id) {
        TeamUser u = this.getById(id);
        if (u == null) throw new ResourceNotFoundException("User not found: " + id);
        return toDTO(u);
    }

    @Override
    public boolean updateUser(Long id, TeamUserDTO dto) {
        TeamUser u = this.getById(id);
        if (u == null) throw new ResourceNotFoundException("User not found: " + id);
        if (StringUtils.hasText(dto.getDisplayName())) u.setDisplayName(dto.getDisplayName());
        if (StringUtils.hasText(dto.getEmail())) u.setEmail(dto.getEmail());
        if (StringUtils.hasText(dto.getStatus())) u.setStatus(dto.getStatus());
        if (StringUtils.hasText(dto.getAvatarUrl())) u.setAvatarUrl(dto.getAvatarUrl());
        u.setUpdateTime(LocalDateTime.now());
        return this.updateById(u);
    }

    @Override
    public boolean deleteUser(Long id) {
        return this.removeById(id);
    }

    private TeamUserDTO toDTO(TeamUser u) {
        TeamUserDTO dto = new TeamUserDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setDisplayName(u.getDisplayName());
        dto.setEmail(u.getEmail());
        dto.setAvatarUrl(u.getAvatarUrl());
        dto.setStatus(u.getStatus());
        dto.setCreateTime(u.getCreateTime());
        dto.setUpdateTime(u.getUpdateTime());
        return dto;
    }
}