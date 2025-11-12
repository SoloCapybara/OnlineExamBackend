package com.dxd.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 答题记录实体类
 * 对应数据库表：answer_record（答题记录表）
 */
@Data
@TableName("answer_record")
public class AnswerRecord {
    @TableId(type = IdType.AUTO)
    private Long recordId; //记录ID
    private Long paperInstanceId; //试卷实例ID
    private Long questionId; //题目ID
    private String studentAnswer; //学生答案
    private Integer isCorrect; //是否正确
    private BigDecimal actualScore; //实际得分
    private String teacherComment; //教师评语
    private Long graderId; //批改教师ID
    private LocalDateTime gradeTime; //批改时间
    private LocalDateTime createTime; //创建时间
    private LocalDateTime updateTime; //更新时间
}

