package com.dxd.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("class")
public class Class {
    @TableId(type = IdType.AUTO)
    private Long classId;
    private String className;
    private String grade;
    private Integer studentCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

