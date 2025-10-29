package com.dxd.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("exam_class")
public class ExamClass {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examId;
    private Long classId;
}

