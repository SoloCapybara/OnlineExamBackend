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
    private Boolean objectiveGraded;  // 客观题是否已批改
    private Boolean subjectiveGraded; // 主观题是否已批改
    private Boolean hasSubjective;    // 是否有主观题
}

