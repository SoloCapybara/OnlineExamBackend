package com.dxd.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam")
public class Exam {
    @TableId(type = IdType.AUTO)
    private Long examId;
    private String examName;
    private String description;
    private Long subjectId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private BigDecimal totalScore;
    private BigDecimal passingScore;
    private Integer questionCount;
    private String status;
    private Long creatorId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

