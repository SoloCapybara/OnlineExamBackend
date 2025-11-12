package com.dxd.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩实体类
 * 对应数据库表：score（成绩表）
 */
@Data
@TableName("score")
public class Score {
    @TableId(type = IdType.AUTO)
    private Long scoreId; //成绩ID
    private Long paperInstanceId; //试卷实例ID
    private Long examId; //考试ID
    private Long studentId; //学生ID
    private Long classId; //班级ID
    private BigDecimal objectiveScore; //客观题得分
    private BigDecimal subjectiveScore; //主观题得分
    private BigDecimal totalScore; //总分
    @TableField("`rank`")  // 排名(为Mysql保留字加反引号)
    private Integer rank; 
    private Integer classRank; //班级排名
    private Integer isPassed; //是否及格
    private LocalDateTime createTime; //创建时间
    private LocalDateTime updateTime; //更新时间
}