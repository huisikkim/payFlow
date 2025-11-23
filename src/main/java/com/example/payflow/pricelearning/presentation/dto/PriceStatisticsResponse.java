package com.example.payflow.pricelearning.presentation.dto;

import com.example.payflow.pricelearning.domain.PriceStatistics;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PriceStatisticsResponse {
    private String itemName;
    private Long averagePrice;
    private Long minPrice;
    private Long maxPrice;
    private Long recentPrice;
    private Long recommendedPrice;
    private Integer dataPoints;
    private Double volatility;
    private String volatilityLevel; // LOW, MEDIUM, HIGH
    
    public static PriceStatisticsResponse from(PriceStatistics stats) {
        String volatilityLevel = determineVolatilityLevel(stats.getVolatility());
        
        return new PriceStatisticsResponse(
            stats.getItemName(),
            stats.getAveragePrice(),
            stats.getMinPrice(),
            stats.getMaxPrice(),
            stats.getRecentPrice(),
            stats.getRecommendedPrice(),
            stats.getDataPoints(),
            stats.getVolatility(),
            volatilityLevel
        );
    }
    
    private static String determineVolatilityLevel(Double volatility) {
        if (volatility < 10.0) return "LOW";
        if (volatility < 30.0) return "MEDIUM";
        return "HIGH";
    }
}
