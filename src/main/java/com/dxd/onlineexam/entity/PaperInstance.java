package com.dxd.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 试卷实例实体类
 * 对应数据库表：paper_instance（试卷实例表）
 */
@Data
@TableName("paper_instance")
public class PaperInstance {
    @TableId(type = IdType.AUTO)
    private Long paperInstanceId; //试卷实例ID
    private Long examId; //考试ID
    private Long studentId; //学生ID
    private LocalDateTime startTime; //实际开始时间
    private LocalDateTime submitTime; //提交时间
    private Integer remainingTime; //剩余时间
    private String status; //状态
    private BigDecimal objectiveScore; //客观题得分
    private BigDecimal subjectiveScore; //主观题得分
    private BigDecimal totalScore; //总分
    private Integer isGraded; //是否已批改
    private LocalDateTime createTime; //创建时间
    private LocalDateTime updateTime; //更新时间
}

