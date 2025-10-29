package com.dxd.onlineexam.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PaperVO {
    private Long examId;
    private Long paperInstanceId;
    private String examName;
    private LocalDateTime startTime;
    private Integer remainingTime;
    private Integer duration;  // 考试时长（分钟）
    private BigDecimal totalScore;  // 总分
    private List<QuestionVO> questions;
    
    @Data
    public static class QuestionVO {
        private Long questionId;
        private Integer questionNumber;
        private String questionType;
        private String questionContent;
        private BigDecimal score;
        private List<OptionVO> options;
    }
    
    @Data
    public static class OptionVO {
        private String optionId;
        private String content;
    }
}

