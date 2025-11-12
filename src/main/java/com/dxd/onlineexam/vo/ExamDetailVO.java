package com.dxd.onlineexam.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试详情VO
 * 用于返回考试的详细信息，包括题目列表和目标班级
 */
@Data
public class ExamDetailVO {
    private Long examId; //考试ID
    private String examName; //考试名称
    private String description; //考试说明
    private Long subjectId; //科目ID
    private String subjectName; //科目名称
    private LocalDateTime startTime; //开始时间
    private LocalDateTime endTime; //结束时间
    private Integer duration; //考试时长（分钟）
    private BigDecimal totalScore; //总分
    private BigDecimal passingScore; //及格分数
    private Integer questionCount; //题目数量
    private String status; //考试状态
    private List<Long> classIds; //目标班级ID列表
    private List<QuestionVO> questions; //题目列表
}

