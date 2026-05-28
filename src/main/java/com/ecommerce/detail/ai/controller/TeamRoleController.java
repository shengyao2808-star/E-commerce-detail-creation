package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.TeamRoleDTO;
import com.ecommerce.detail.ai.service.TeamRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/team/roles")
public class TeamRoleController {

    @Autowired
    private TeamRoleService teamRoleService;

    @GetMapping("/list")
    public Result<PageResult<TeamRoleDTO>> listRoles(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(teamRoleService.listRoles(pageNum, pageSize));
    }

    @GetMapping("/all")
    public Result<List<TeamRoleDTO>> listAllRoles() {
        return Result.success(teamRoleService.listAllRoles());
    }

    @PostMapping
    public Result<Long> createRole(@RequestBody TeamRoleDTO dto) {
        return Result.success(teamRoleService.createRole(dto));
    }

    @GetMapping("/{id}")
    public Result<TeamRoleDTO> getRoleById(@PathVariable Long id) {
        return Result.success(teamRoleService.getRoleById(id));
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateRole(@PathVariable Long id, @RequestBody TeamRoleDTO dto) {
        return Result.success(teamRoleService.updateRole(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteRole(@PathVariable Long id) {
        return Result.success(teamRoleService.deleteRole(id));
    }
}