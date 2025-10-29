package com.dxd.onlineexam.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamDetailVO {
    private Long examId;
    private String examName;
    private String description;
    private Long subjectId;
    private String subjectName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private BigDecimal totalScore;
    private BigDecimal passingScore;
    private Integer questionCount;
    private String status;
    private List<Long> classIds;
    private List<QuestionVO> questions;
}

