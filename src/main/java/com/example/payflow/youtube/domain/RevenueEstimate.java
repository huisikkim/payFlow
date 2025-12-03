package com.example.payflow.youtube.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueEstimate {
    private Long minRevenue;        // 최소 예상 수익 (원)
    private Long maxRevenue;        // 최대 예상 수익 (원)
    private Long avgRevenue;        // 평균 예상 수익 (원)
    private Integer cpmMin;         // 최소 CPM (원)
    private Integer cpmMax;         // 최대 CPM (원)
    private String categoryName;    // 카테고리명
    private Integer potentialScore; // 수익 잠재력 점수 (0-100)
}
