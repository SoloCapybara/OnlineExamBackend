package com.dxd.onlineexam.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 成绩详情VO
 * 用于返回学生成绩的详细信息，包括各题目的答题情况
 */
@Data
public class ScoreDetailVO {
    private String examName; //考试名称
    private String examDate; //考试日期
    private BigDecimal totalScore; //总分
    private BigDecimal objectiveScore; //客观题得分
    private BigDecimal subjectiveScore; //主观题得分
    private Integer rank; //排名
    private Integer classRank; //班级排名
    private Integer totalStudents; //总学生数
    private LocalDateTime submitTime; //提交时间
    private List<QuestionDetail> questions; //题目详情列表
    
    /**
     * 题目详情内部类
     */
    @Data
    public static class QuestionDetail {
        private Integer questionNumber; //题目序号
        private String questionContent; //题目内容
        private String questionType; //题目类型
        private String typeName; //题目类型名称
        private BigDecimal score; //分值
        private BigDecimal maxScore; //满分
        private String studentAnswer; //学生答案
        private String correctAnswer; //正确答案
        private String referenceAnswer; //主观题参考答案
        private Boolean isCorrect; //是否正确
        private BigDecimal actualScore; //实际得分
        private String teacherComment; //教师评语
        private String analysis; //答案解析
        private String comment; //教师评语（备用字段）
        private List<OptionDetail> options; //选项信息（选择题使用）
        
        /**
         * 选项详情内部类
         */
        @Data
        public static class OptionDetail {
            private String label; //选项标识（A/B/C/D等）
            private String content; //选项内容
        }
    }
}

