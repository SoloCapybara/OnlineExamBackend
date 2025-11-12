package com.dxd.onlineexam.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 题目VO（教师端）
 * 用于返回题目的详细信息，包括选项、答案、解析等
 */
@Data
public class QuestionVO {
    private Long questionId; //题目ID
    private String questionCode; //题目编号
    private String content; //题目内容
    private String type; //题目类型：single_choice/multiple_choice/judge/essay
    private String typeName; //题目类型名称
    private Long subjectId; //科目ID
    private String subject; //科目名称
    private String difficulty; //难度：easy/medium/hard
    private String difficultyName; //难度名称
    private BigDecimal score; //分值
    private List<OptionVO> options; //选项列表（选择题使用）
    private String correctAnswer; //正确答案（客观题使用）
    private String analysis; //答案解析
    private String referenceAnswer; //参考答案（主观题使用）
    
    /**
     * 选项VO内部类
     */
    @Data
    public static class OptionVO {
        private String optionId; //选项ID（A/B/C/D等）
        private String content; //选项内容
    }
}
