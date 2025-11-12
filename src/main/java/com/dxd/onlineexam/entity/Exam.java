package com.dxd.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考试实体类
 * 对应数据库表：exam（考试表）
 */
@Data
@TableName("exam")
public class Exam {
    @TableId(type = IdType.AUTO)
    private Long examId; //考试ID
    private String examName; //考试名称
    private String description; //考试说明
    private Long subjectId; //科目ID
    private LocalDateTime startTime; //开始时间
    private LocalDateTime endTime; //结束时间
    private Integer duration; //考试时长
    private BigDecimal totalScore; //总分
    private BigDecimal passingScore; //及格分数
    private Integer questionCount; //题目数量
    private String status; //状态
    private Long creatorId; //创建者ID
    private LocalDateTime createTime; //创建时间
    private LocalDateTime updateTime; //更新时间
}

