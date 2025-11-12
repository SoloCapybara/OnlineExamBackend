package com.dxd.onlineexam.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 成绩列表项VO
 * 用于返回学生成绩列表，包含基本的成绩信息
 */
@Data
public class ScoreVO {
    private Long examId; //考试ID
    private String examName; //考试名称
    private String examDate; //考试日期
    private BigDecimal objectiveScore; //客观题得分
    private BigDecimal subjectiveScore; //主观题得分
    private BigDecimal totalScore; //总分
    private String rank; //排名
    private Boolean isGraded; //是否已批改
    private Boolean objectiveGraded; //客观题是否已批改
    private Boolean subjectiveGraded; //主观题是否已批改
    private Boolean hasSubjective; //是否有主观题
}

