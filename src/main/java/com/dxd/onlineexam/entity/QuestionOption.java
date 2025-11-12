package com.dxd.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 题目选项实体类
 * 对应数据库表：question_option（题目选项表）
 */
@Data
@TableName("question_option")
public class QuestionOption {
    @TableId(type = IdType.AUTO)
    private Long optionId; //选项ID
    private Long questionId; //题目ID
    private String optionLabel; //选项标识
    private String content; //选项内容
    private Integer sortOrder; //排序
}

