package com.example.payflow.youtube.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * CTR(Click-Through Rate) 추정
 */
@Getter
@Builder
public class CtrEstimate {
    private Double estimatedCtr;  // 예상 CTR (%)
    private String ctrLevel;  // "높음", "보통", "낮음"
    
    // CTR 영향 요소
    private List<CtrFactor> factors;
    
    // 개선 제안
    private List<String> improvements;
    
    @Getter
    @Builder
    public static class CtrFactor {
        private String factor;
        private Integer score;  // 0-100
        private String impact;  // "긍정적", "부정적", "중립"
        private String description;
    }
}
