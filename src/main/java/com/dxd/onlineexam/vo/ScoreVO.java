package com.dxd.onlineexam.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ScoreVO {
    private Long examId;
    private String examName;
    private String examDate;
    private BigDecimal objectiveScore;
    private BigDecimal subjectiveScore;
    private BigDecimal totalScore;
    private String rank;
    private Boolean isGraded;
}

