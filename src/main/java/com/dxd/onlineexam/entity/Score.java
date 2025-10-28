package com.dxd.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("score")
public class Score {
    @TableId(type = IdType.AUTO)
    private Long scoreId;
    private Long paperInstanceId;
    private Long examId;
    private Long studentId;
    private Long classId;
    private BigDecimal objectiveScore;
    private BigDecimal subjectiveScore;
    private BigDecimal totalScore;
    private Integer rank;
    private Integer classRank;
    private Integer isPassed;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

