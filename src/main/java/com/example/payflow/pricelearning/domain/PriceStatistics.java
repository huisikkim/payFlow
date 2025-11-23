package com.example.payflow.pricelearning.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PriceStatistics {
    private String itemName;
    private Long averagePrice;
    private Long minPrice;
    private Long maxPrice;
    private Long recentPrice;
    private Long recommendedPrice;
    private Integer dataPoints;
    private Double volatility; // 변동성 (표준편차 / 평균)
}
