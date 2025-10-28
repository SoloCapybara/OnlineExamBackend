package com.dxd.onlineexam.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GradingDetailVO {
    private Long paperInstanceId;
    private String studentName;
    private String examName;
    private BigDecimal objectiveScore;
    private List<SubjectiveQuestion> subjectiveQuestions;
    
    @Data
    public static class SubjectiveQuestion {
        private Long questionId;
        private Integer questionNumber;
        private String content;
        private BigDecimal score;
        private String studentAnswer;
        private String referenceAnswer;
        private BigDecimal actualScore;
        private String comment;
    }
}

