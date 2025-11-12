package com.dxd.onlineexam.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 题目请求DTO
 */
@Data
public class QuestionRequest {
    private String content; //题目内容
    private String type; //题目类型：single_choice/multiple_choice/judge/essay
    private Long subjectId; //科目ID
    private String difficulty; //难度：easy/medium/hard
    private BigDecimal score; //默认分值
    private List<OptionItem> options; //选项列表（选择题使用）
    private String correctAnswer; //正确答案（客观题使用）
    private String analysis; //答案解析
    private String referenceAnswer; //参考答案（主观题使用）
    
    /**
     * 选项项
     */
    @Data
    public static class OptionItem {
        private String optionId; //选项标识（A/B/C/D等）
        private String content; //选项内容
    }
}

