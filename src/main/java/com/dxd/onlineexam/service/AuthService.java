package com.dxd.onlineexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dxd.onlineexam.entity.User;
import com.dxd.onlineexam.mapper.UserMapper;
import com.dxd.onlineexam.vo.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserMapper userMapper;

    /**
     * 用户登录
     */
    public LoginResponse login(String username, String password, String role) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username)
               .eq("password", password)
               .eq("role", role)
               .eq("status", 1);
        
        User user = userMapper.selectOne(wrapper);
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 生成简单的token（实训项目用UUID即可）
        String token = UUID.randomUUID().toString();
        
        return LoginResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    /**
     * 获取当前用户信息
     */
    public User getCurrentUser(Long userId) {
        return userMapper.selectById(userId);
    }
}

