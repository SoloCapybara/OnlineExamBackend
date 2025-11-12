package com.dxd.onlineexam.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 批改详情VO
 * 用于返回需要批改的主观题试卷详情
 */
@Data
public class GradingDetailVO {
    private Long paperInstanceId; //试卷实例ID
    private String studentName; //学生姓名
    private String examName; //考试名称
    private BigDecimal objectiveScore; //客观题得分
    private List<SubjectiveQuestion> subjectiveQuestions; //主观题列表
    
    /**
     * 主观题内部类
     */
    @Data
    public static class SubjectiveQuestion {
        private Long questionId; //题目ID
        private Integer questionNumber; //题目序号
        private String content; //题目内容
        private BigDecimal score; //分值
        private String studentAnswer; //学生答案
        private String referenceAnswer; //参考答案
        private BigDecimal actualScore; //实际得分（批改后填写）
        private String comment; //评语（批改后填写）
    }
}

