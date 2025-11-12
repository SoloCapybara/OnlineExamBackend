package com.dxd.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 科目实体类
 * 对应数据库表：subject（科目表）
 */
@Data
@TableName("subject")
public class Subject {
    @TableId(type = IdType.AUTO)
    private Long subjectId; //科目ID
    private String subjectName; //科目名称
    private String description; //科目描述
    private LocalDateTime createTime; //创建时间
}

