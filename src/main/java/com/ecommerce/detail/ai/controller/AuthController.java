package com.ecommerce.detail.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.detail.ai.common.Result;
import com.ecommerce.detail.ai.config.JwtUtil;
import com.ecommerce.detail.ai.entity.UserAccount;
import com.ecommerce.detail.ai.mapper.UserAccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return Result.badRequest("Username and password are required");
        }

        UserAccount user = userAccountMapper.selectOne(
                new LambdaQueryWrapper<UserAccount>().eq(UserAccount::getUsername, username));
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            return Result.error(401, "Invalid username or password");
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            return Result.error(403, "Account is disabled");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return Result.success(Map.of(
                "token", token,
                "username", user.getUsername(),
                "displayName", user.getDisplayName() != null ? user.getDisplayName() : user.getUsername(),
                "role", user.getRole() != null ? user.getRole() : "USER"
        ));
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return Result.badRequest("Username and password are required");
        }
        if (username.length() < 3 || username.length() > 50) {
            return Result.badRequest("Username must be 3-50 characters");
        }
        if (password.length() < 6) {
            return Result.badRequest("Password must be at least 6 characters");
        }

        UserAccount existing = userAccountMapper.selectOne(
                new LambdaQueryWrapper<UserAccount>().eq(UserAccount::getUsername, username));
        if (existing != null) {
            return Result.badRequest("Username already exists");
        }

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setDisplayName(username);
        user.setRole("USER");
        user.setStatus("ACTIVE");
        userAccountMapper.insert(user);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return Result.success(Map.of(
                "token", token,
                "username", user.getUsername(),
                "role", user.getRole()
        ));
    }
}