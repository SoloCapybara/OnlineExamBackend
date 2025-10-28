package com.dxd.onlineexam.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExamVO {
    private Long examId;
    private String examName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private BigDecimal totalScore;
    private String status;
    private Boolean isSubmitted;
    private BigDecimal score;
}

