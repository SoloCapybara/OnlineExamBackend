package com.dxd.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("subject")
public class Subject {
    @TableId(type = IdType.AUTO)
    private Long subjectId;
    private String subjectName;
    private String description;
    private LocalDateTime createTime;
}

