package com.dxd.onlineexam.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 待批改主观题试卷VO
 * 用于返回需要批改主观题的试卷列表
 */
@Data
public class PendingPaperVO {
    private Long paperInstanceId; //试卷实例ID
    private Long studentId; //学生ID
    private String studentName; //学生姓名
    private Long examId; //考试ID
    private String examName; //考试名称
    private LocalDateTime submitTime; //提交时间
    private BigDecimal objectiveScore; //客观题得分
    private Integer subjectiveQuestionCount; //主观题数量
}

