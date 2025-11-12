package com.dxd.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 班级实体类
 * 对应数据库表：class（班级表）
 */
@Data
@TableName("class")
public class Class {
    @TableId(type = IdType.AUTO)
    private Long classId; //班级ID
    private String className; //班级名称
    private String grade; //年级
    private Integer studentCount; //学生人数
    private LocalDateTime createTime; //创建时间
    private LocalDateTime updateTime; //更新时间
}

