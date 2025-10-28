package com.dxd.onlineexam.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class ExamRequest {
    private String examName;
    private String description;
    private Long subjectId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private BigDecimal passingScore;
    private List<Long> questionIds;
    private Map<Long, BigDecimal> questionScores;
    private List<Long> targetClassIds;
}

