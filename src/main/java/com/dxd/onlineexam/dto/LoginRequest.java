package com.dxd.onlineexam.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 */
@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username; //用户名
    
    @NotBlank(message = "密码不能为空")
    private String password; //密码
    
    @NotBlank(message = "角色不能为空")
    private String role; //角色：teacher/student
}

