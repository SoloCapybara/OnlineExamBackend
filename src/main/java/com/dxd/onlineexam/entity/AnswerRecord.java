package com.dxd.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("answer_record")
public class AnswerRecord {
    @TableId(type = IdType.AUTO)
    private Long recordId;
    private Long paperInstanceId;
    private Long questionId;
    private String studentAnswer;
    private Integer isCorrect;
    private BigDecimal actualScore;
    private String teacherComment;
    private Long graderId;
    private LocalDateTime gradeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

