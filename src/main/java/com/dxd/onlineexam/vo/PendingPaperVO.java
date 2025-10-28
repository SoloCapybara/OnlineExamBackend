package com.dxd.onlineexam.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PendingPaperVO {
    private Long paperInstanceId;
    private Long studentId;
    private String studentName;
    private Long examId;
    private String examName;
    private LocalDateTime submitTime;
    private BigDecimal objectiveScore;
    private Integer subjectiveQuestionCount;
}

