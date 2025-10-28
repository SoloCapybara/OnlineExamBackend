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
    private String subject;
    private String difficulty;
    private String difficultyName;
    private BigDecimal score;
    private List<OptionVO> options;
    private String correctAnswer;
    private String analysis;
    
    @Data
    public static class OptionVO {
        private String optionId;
        private String content;
    }
}
