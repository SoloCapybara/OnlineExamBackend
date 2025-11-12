package com.dxd.onlineexam.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考试列表项VO（教师端）
 * 用于返回教师可见的考试列表信息，包含统计信息
 */
@Data
public class ExamListVO {
    private Long examId; //考试ID
    private String examName; //考试名称
    private LocalDateTime startTime; //开始时间
    private LocalDateTime endTime; //结束时间
    private Integer duration; //考试时长（分钟）
    private BigDecimal totalScore; //总分
    private Integer participantCount; //参与人数
    private Integer submittedCount; //已提交人数
    private Integer gradedCount; //已批改人数
    private String status; //考试状态
    private LocalDateTime createTime; //创建时间
}

