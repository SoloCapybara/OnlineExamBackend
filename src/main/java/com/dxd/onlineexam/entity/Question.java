package com.dxd.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long questionId;
    private String questionCode;
    private String content;
    private String type;
    private Long subjectId;
    private String difficulty;
    private BigDecimal defaultScore;
    private String correctAnswer;
    private String analysis;
    private String referenceAnswer;
    private Long creatorId;
    private Integer useCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

