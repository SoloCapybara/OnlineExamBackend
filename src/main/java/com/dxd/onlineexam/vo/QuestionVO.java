package com.dxd.onlineexam.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class QuestionVO {
    private Long questionId;
    private String questionCode;
    private String content;
    private String type;
    private String typeName;
    private Long subjectId;  // 添加科目ID
    private String subject;
    private String difficulty;
    private String difficultyName;
    private BigDecimal score;
    private List<OptionVO> options;
    private String correctAnswer;
    private String analysis;
    private String referenceAnswer;  // 添加主观题参考答案
    
    @Data
    public static class OptionVO {
        private String optionId;
        private String content;
    }
}
