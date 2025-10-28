package com.dxd.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("question_option")
public class QuestionOption {
    @TableId(type = IdType.AUTO)
    private Long optionId;
    private Long questionId;
    private String optionLabel;
    private String content;
    private Integer sortOrder;
}

