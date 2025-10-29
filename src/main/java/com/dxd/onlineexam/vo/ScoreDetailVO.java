package com.dxd.onlineexam.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScoreDetailVO {
    private String examName;
    private String examDate;  // 添加考试日期
    private BigDecimal totalScore;
    private BigDecimal objectiveScore;
    private BigDecimal subjectiveScore;
    private Integer rank;
    private Integer classRank;  // 添加班级排名
    private Integer totalStudents;
    private LocalDateTime submitTime;
    private List<QuestionDetail> questions;
    
    @Data
    public static class QuestionDetail {
        private Integer questionNumber;
        private String questionContent;
        private String questionType;
        private String typeName;  // 添加题目类型名称
        private BigDecimal score;
        private BigDecimal maxScore;  // 添加满分
        private String studentAnswer;
        private String correctAnswer;
        private Boolean isCorrect;
        private BigDecimal actualScore;
        private String teacherComment;
        private String analysis;  // 添加答案解析
        private String comment;   // 添加教师评语
        private List<OptionDetail> options;  // 添加选项信息
        
        @Data
        public static class OptionDetail {
            private String label;
            private String content;
        }
    }
}

