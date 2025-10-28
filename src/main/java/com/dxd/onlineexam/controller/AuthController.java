package com.dxd.onlineexam.controller;

import com.dxd.onlineexam.common.Result;
import com.dxd.onlineexam.dto.LoginRequest;
import com.dxd.onlineexam.entity.User;
import com.dxd.onlineexam.service.AuthService;
import com.dxd.onlineexam.vo.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(
            request.getUsername(),
            request.getPassword(),
            request.getRole()
        );
        return Result.success(response);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        // 简单实现，直接返回成功
        return Result.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current-user")
    public Result<User> getCurrentUser(@RequestParam Long userId) {
        User user = authService.getCurrentUser(userId);
        return Result.success(user);
    }
}

