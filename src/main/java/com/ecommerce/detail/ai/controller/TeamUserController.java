package com.ecommerce.detail.ai.controller;

import com.ecommerce.detail.ai.common.PageResult;
import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.dto.TeamUserCreateRequest;
import com.ecommerce.detail.ai.dto.TeamUserDTO;
import com.ecommerce.detail.ai.service.TeamUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/team/users")
public class TeamUserController {

    @Autowired
    private TeamUserService teamUserService;

    @GetMapping("/list")
    public Result<PageResult<TeamUserDTO>> listUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.success(teamUserService.listUsers(pageNum, pageSize, keyword, status));
    }

    @PostMapping
    public Result<Long> createUser(@Valid @RequestBody TeamUserCreateRequest request) {
        return Result.success(teamUserService.createUser(request));
    }

    @GetMapping("/{id}")
    public Result<TeamUserDTO> getUserById(@PathVariable Long id) {
        return Result.success(teamUserService.getUserById(id));
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateUser(@PathVariable Long id, @RequestBody TeamUserDTO dto) {
        return Result.success(teamUserService.updateUser(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        return Result.success(teamUserService.deleteUser(id));
    }
}