package com.dxd.onlineexam.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 批改请求DTO
 */
@Data
public class GradingRequest {
    private List<GradeItem> grades; //批改项列表
    
    /**
     * 批改项
     */
    @Data
    public static class GradeItem {
        private Long questionId; //题目ID
        private BigDecimal actualScore; //实际得分
        private String comment; //评语
    }
}

