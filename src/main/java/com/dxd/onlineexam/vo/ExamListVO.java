package com.dxd.onlineexam.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExamListVO {
    private Long examId;
    private String examName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private BigDecimal totalScore;
    private Integer participantCount;
    private Integer submittedCount;
    private Integer gradedCount;
    private String status;
    private LocalDateTime createTime;
}

