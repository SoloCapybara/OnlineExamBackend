package com.dxd.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long userId;
    private String username;
    private String password;
    private String realName;
    private String role;
    private String email;
    private String phone;
    private String avatar;
    private Long classId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

