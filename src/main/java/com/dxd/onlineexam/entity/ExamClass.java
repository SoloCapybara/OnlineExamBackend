package com.dxd.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 考试班级关联实体类
 * 对应数据库表：exam_class（考试班级关联表，中间表）
 * 用于关联考试和班级，表示哪些班级可以参加该考试
 */
@Data
@TableName("exam_class")
public class ExamClass {
    @TableId(type = IdType.AUTO)
    private Long id; //主键
    private Long examId; //考试ID
    private Long classId; //班级ID
}

