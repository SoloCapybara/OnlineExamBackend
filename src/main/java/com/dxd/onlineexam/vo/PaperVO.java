package com.dxd.onlineexam.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 试卷VO
 * 用于返回学生开始考试后的试卷信息，包含题目列表
 */
@Data
public class PaperVO {
    private Long examId; //考试ID
    private Long paperInstanceId; //试卷实例ID
    private String examName; //考试名称
    private LocalDateTime startTime; //开始时间
    private Integer remainingTime; //剩余时间（秒）
    private Integer duration; //考试时长（分钟）
    private BigDecimal totalScore; //总分
    private List<QuestionVO> questions; //题目列表
    
    /**
     * 题目VO内部类
     */
    @Data
    public static class QuestionVO {
        private Long questionId; //题目ID
        private Integer questionNumber; //题目序号
        private String questionType; //题目类型
        private String questionContent; //题目内容
        private BigDecimal score; //分值
        private List<OptionVO> options; //选项列表（选择题使用）
    }
    
    /**
     * 选项VO内部类
     */
    @Data
    public static class OptionVO {
        private String optionId; //选项ID（A/B/C/D等）
        private String content; //选项内容
    }
}

