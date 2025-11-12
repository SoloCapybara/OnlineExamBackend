package com.dxd.onlineexam.dto;

import lombok.Data;

/**
 * 保存答案请求DTO
 */
@Data
public class SaveAnswerRequest {
    private Long paperInstanceId; //试卷实例ID
    private Long questionId; //题目ID
    private String answer; //学生答案
}

