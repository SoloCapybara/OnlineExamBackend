package com.dxd.onlineexam.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExamDetailVO {
    private Long examId;
    private String examName;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private BigDecimal totalScore;
    private BigDecimal passingScore;
    private Integer questionCount;
    private String status;
}

