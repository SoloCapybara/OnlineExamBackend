package com.dxd.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 题目实体类
 * 对应数据库表：question（题目表）
 */
@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long questionId; //题目ID
    private String questionCode; //题目编号
    private String content; //题目内容
    private String type; //题目类型
    private Long subjectId; //科目ID
    private String difficulty; //难度
    private BigDecimal defaultScore; //默认分值
    private String correctAnswer; //正确答案
    private String analysis; //答案解析
    private String referenceAnswer; //参考答案
    private Long creatorId; //创建者ID
    private Integer useCount; //使用次数
    private Integer status; //状态:0禁用，1启用
    private LocalDateTime createTime; //创建时间
    private LocalDateTime updateTime; //更新时间
}

