package com.dxd.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表：user（用户表）
 */
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long userId; //用户ID
    private String username; //用户名
    private String password; //密码
    private String realName; //真实姓名
    private String role; //角色:teacher/student
    private String email; //电子邮箱
    private Long classId; //所属班级id
    private Integer status; //状态:0禁用，1启用
    private LocalDateTime createTime; //创建时间
    private LocalDateTime updateTime; //更新时间
}

