package com.dxd.onlineexam.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PendingExamVO {
    private Long examId;
    private String examName;
    private LocalDateTime endTime;
    private Integer submittedCount;
    private Integer ungradedCount;
}

