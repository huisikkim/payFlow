package com.example.payflow.menu.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@AllArgsConstructor
public class MarginSimulation {
    
    private Long menuId;
    private String menuName;
    private Long totalCost; // 총 원가
    private Long currentSellingPrice; // 현재 판매가
    private BigDecimal currentMarginRate; // 현재 마진율
    private Long targetSellingPrice; // 목표 판매가
    private BigDecimal targetMarginRate; // 목표 마진율
    private Long priceDifference; // 가격 차이
    private BigDecimal marginDifference; // 마진율 차이
    
    public static MarginSimulation simulateByPrice(MenuCostAnalysis analysis, Long targetPrice) {
        Long totalCost = analysis.getTotalCost();
        Long grossProfit = targetPrice - totalCost;
        
        BigDecimal targetMarginRate = targetPrice > 0
            ? BigDecimal.valueOf(grossProfit)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(targetPrice), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        Long priceDiff = targetPrice - analysis.getSellingPrice();
        BigDecimal marginDiff = targetMarginRate.subtract(analysis.getMarginRate());
        
        return new MarginSimulation(
            analysis.getMenuId(),
            analysis.getMenuName(),
            totalCost,
            analysis.getSellingPrice(),
            analysis.getMarginRate(),
            targetPrice,
            targetMarginRate,
            priceDiff,
            marginDiff
        );
    }
    
    public static MarginSimulation simulateByMargin(MenuCostAnalysis analysis, BigDecimal targetMargin) {
        Long totalCost = analysis.getTotalCost();
        
        // 목표 판매가 = 원가 / (1 - 목표마진율/100)
        BigDecimal targetPrice = targetMargin.compareTo(BigDecimal.valueOf(100)) >= 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(totalCost)
                .divide(BigDecimal.ONE.subtract(targetMargin.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)), 0, RoundingMode.HALF_UP);
        
        Long targetPriceLong = targetPrice.longValue();
        Long priceDiff = targetPriceLong - analysis.getSellingPrice();
        BigDecimal marginDiff = targetMargin.subtract(analysis.getMarginRate());
        
        return new MarginSimulation(
            analysis.getMenuId(),
            analysis.getMenuName(),
            totalCost,
            analysis.getSellingPrice(),
            analysis.getMarginRate(),
            targetPriceLong,
            targetMargin,
            priceDiff,
            marginDiff
        );
    }
}
