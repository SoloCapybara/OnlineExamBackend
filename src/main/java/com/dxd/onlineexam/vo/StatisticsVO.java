package com.dxd.onlineexam.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class StatisticsVO {
    private String examName;
    private Integer participantCount;
    private Integer submittedCount;
    private BigDecimal avgScore;
    private BigDecimal maxScore;
    private BigDecimal minScore;
    private BigDecimal passRate;
    private BigDecimal excellentRate;
    private List<ScoreDistribution> scoreDistribution;
    private List<QuestionAnalysis> questionAnalysis;
    
    @Data
    public static class ScoreDistribution {
        private String range;
        private Integer count;
    }
    
    @Data
    public static class QuestionAnalysis {
        private Integer questionNumber;
        private String questionType;
        private BigDecimal correctRate;
        private BigDecimal avgScore;
        private BigDecimal fullScore;
    }
}

