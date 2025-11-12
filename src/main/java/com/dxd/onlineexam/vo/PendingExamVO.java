package com.dxd.onlineexam.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 待自动判卷考试VO
 * 用于返回需要自动判卷的考试列表
 */
@Data
public class PendingExamVO {
    private Long examId; //考试ID
    private String examName; //考试名称
    private LocalDateTime endTime; //结束时间
    private Integer submittedCount; //已提交人数
    private Integer ungradedCount; //未批改人数
}

