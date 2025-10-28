package com.dxd.onlineexam.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScoreDetailVO {
    private String examName;
    private BigDecimal totalScore;
    private BigDecimal objectiveScore;
    private BigDecimal subjectiveScore;
    private Integer rank;
    private Integer totalStudents;
    private LocalDateTime submitTime;
    private List<QuestionDetail> questions;
    
    @Data
    public static class QuestionDetail {
        private Integer questionNumber;
        private String questionContent;
        private String questionType;
        private BigDecimal score;
        private String studentAnswer;
        private String correctAnswer;
        private Boolean isCorrect;
        private BigDecimal actualScore;
        private String teacherComment;
    }
}

