package com.example.payflow.sourcing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KeywordAnalysisRequest {
    
    @NotBlank(message = "키워드는 필수입니다")
    private String keyword;
    
    private String platform = "ALL"; // NAVER, COUPANG, ALL
}
