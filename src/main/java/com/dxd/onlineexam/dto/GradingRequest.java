package com.dxd.onlineexam.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GradingRequest {
    private List<GradeItem> grades;
    
    @Data
    public static class GradeItem {
        private Long questionId;
        private BigDecimal actualScore;
        private String comment;
    }
}

