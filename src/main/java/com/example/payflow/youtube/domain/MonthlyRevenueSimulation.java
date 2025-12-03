package com.example.payflow.youtube.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRevenueSimulation {
    private Integer videosPerMonth;      // 월 영상 개수
    private Long monthlyMinRevenue;      // 월 최소 수익 (원)
    private Long monthlyMaxRevenue;      // 월 최대 수익 (원)
    private Long monthlyAvgRevenue;      // 월 평균 수익 (원)
    private String growthPotential;      // 성장 가능성
    private Integer potentialScore;      // 잠재력 점수
}
