package com.dxd.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("paper_instance")
public class PaperInstance {
    @TableId(type = IdType.AUTO)
    private Long paperInstanceId;
    private Long examId;
    private Long studentId;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private Integer remainingTime;
    private String status;
    private BigDecimal objectiveScore;
    private BigDecimal subjectiveScore;
    private BigDecimal totalScore;
    private Integer isGraded;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

