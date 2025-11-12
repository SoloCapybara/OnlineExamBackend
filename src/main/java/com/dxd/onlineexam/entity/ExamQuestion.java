package com.dxd.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 考试题目关联实体类
 * 对应数据库表：exam_question（考试题目关联表，中间表）
 * 用于关联考试和题目，记录题目在考试中的序号和分值
 */
@Data
@TableName("exam_question")
public class ExamQuestion {
    @TableId(type = IdType.AUTO)
    private Long id; //主键ID
    private Long examId; //考试ID
    private Long questionId; //题目ID
    private Integer questionNumber; //题目序号
    private BigDecimal score; //该题分值
}

