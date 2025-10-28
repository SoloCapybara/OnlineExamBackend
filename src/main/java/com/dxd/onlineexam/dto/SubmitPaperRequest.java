package com.dxd.onlineexam.dto;

import lombok.Data;
import java.util.List;

@Data
public class SubmitPaperRequest {
    private Long paperInstanceId;
    private List<AnswerItem> answers;
    
    @Data
    public static class AnswerItem {
        private Long questionId;
        private String answer;
    }
}

