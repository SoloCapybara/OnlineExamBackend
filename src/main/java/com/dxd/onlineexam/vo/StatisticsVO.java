package com.dxd.onlineexam.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 考试统计数据VO
 * 用于返回考试的统计信息，包括分数分布和题目分析
 */
@Data
public class StatisticsVO {
    private String examName; //考试名称
    private Integer participantCount; //参与人数
    private Integer submittedCount; //已提交人数
    private BigDecimal avgScore; //平均分
    private BigDecimal maxScore; //最高分
    private BigDecimal minScore; //最低分
    private BigDecimal passRate; //及格率（百分比）
    private BigDecimal excellentRate; //优秀率（百分比）
    private List<ScoreDistribution> scoreDistribution; //分数分布列表
    private List<QuestionAnalysis> questionAnalysis; //题目分析列表
    
    /**
     * 分数分布内部类
     */
    @Data
    public static class ScoreDistribution {
        private String range; //分数区间（如 "0-60"）
        private Integer count; //该区间的人数
    }
    
    /**
     * 题目分析内部类
     */
    @Data
    public static class QuestionAnalysis {
        private Integer questionNumber; //题目序号
        private String questionType; //题目类型
        private BigDecimal correctRate; //正确率（百分比）
        private BigDecimal avgScore; //平均得分
        private BigDecimal fullScore; //满分
    }
}

