package com.dxd.onlineexam.dto;

import lombok.Data;
import java.util.List;

/**
 * 提交试卷请求DTO
 */
@Data
public class SubmitPaperRequest {
    private Long paperInstanceId; //试卷实例ID
    private List<AnswerItem> answers; //答案列表
    
    /**
     * 答案项
     */
    @Data
    public static class AnswerItem {
        private Long questionId; //题目ID
        private String answer; //学生答案
    }
}

