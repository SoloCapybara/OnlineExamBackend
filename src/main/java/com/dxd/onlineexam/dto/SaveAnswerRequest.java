package com.dxd.onlineexam.dto;

import lombok.Data;

@Data
public class SaveAnswerRequest {
    private Long paperInstanceId;
    private Long questionId;
    private String answer;
}

