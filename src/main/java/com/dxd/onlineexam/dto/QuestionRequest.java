package com.dxd.onlineexam.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class QuestionRequest {
    private String content;
    private String type;
    private Long subjectId;
    private String difficulty;
    private BigDecimal score;
    private List<OptionItem> options;
    private String correctAnswer;
    private String analysis;
    private String referenceAnswer;
    
    @Data
    public static class OptionItem {
        private String optionId;
        private String content;
    }
}

