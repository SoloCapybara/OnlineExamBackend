package com.dxd.onlineexam.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 登录响应VO
 * 用于返回用户登录后的响应信息
 */
@Data
@Builder
public class LoginResponse {
    private String token; //认证令牌
    private Long userId; //用户ID
    private String username; //用户名
    private String role; //角色：teacher/student
}

