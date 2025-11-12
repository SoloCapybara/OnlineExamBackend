package com.dxd.onlineexam.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 考试请求DTO
 */
@Data
public class ExamRequest {
    private String examName; //考试名称
    private String description; //考试说明
    private Long subjectId; //科目ID
    private LocalDateTime startTime; //开始时间
    private LocalDateTime endTime; //结束时间
    private Integer duration; //考试时长（分钟）
    private BigDecimal passingScore; //及格分数
    private List<Long> questionIds; //题目ID列表
    private Map<Long, BigDecimal> questionScores; //题目分值映射（题目ID -> 分值）
    private List<Long> targetClassIds; //目标班级ID列表
}

