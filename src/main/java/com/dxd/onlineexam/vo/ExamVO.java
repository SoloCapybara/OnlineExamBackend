package com.dxd.onlineexam.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考试列表项VO（学生端）
 * 用于返回学生可见的考试列表信息
 */
@Data
public class ExamVO {
    private Long examId; //考试ID
    private String examName; //考试名称
    private LocalDateTime startTime; //开始时间
    private LocalDateTime endTime; //结束时间
    private Integer duration; //考试时长（分钟）
    private BigDecimal totalScore; //总分
    private String status; //考试状态：pending/ongoing/completed
    private String paperStatus; //试卷状态：not_started/ongoing/submitted
    private Boolean isSubmitted; //是否已提交
    private BigDecimal score; //得分（已提交且有成绩时才有）
}

