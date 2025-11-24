package com.example.payflow.menu.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Getter
@AllArgsConstructor
public class MenuCostAnalysis {
    
    private Long menuId;
    private String menuName;
    private Long totalCost; // 총 원가
    private Long sellingPrice; // 판매가
    private Long grossProfit; // 매출 총이익
    private BigDecimal marginRate; // 마진율 (%)
    private BigDecimal markupRate; // 마크업율 (%)
    private List<IngredientCost> ingredientCosts; // 재료별 원가
    
    @Getter
    @AllArgsConstructor
    public static class IngredientCost {
        private String ingredientName;
        private BigDecimal quantity;
        private String unit;
        private Long unitPrice; // 단가
        private Long cost; // 원가 (quantity × unitPrice)
        private BigDecimal costRatio; // 원가 비율 (%)
    }
    
    public static MenuCostAnalysis calculate(Menu menu, List<IngredientCost> ingredientCosts) {
        Long totalCost = ingredientCosts.stream()
            .mapToLong(IngredientCost::getCost)
            .sum();
        
        Long sellingPrice = menu.getSellingPrice();
        Long grossProfit = sellingPrice - totalCost;
        
        // 마진율 = (판매가 - 원가) / 판매가 × 100
        BigDecimal marginRate = sellingPrice > 0 
            ? BigDecimal.valueOf(grossProfit)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(sellingPrice), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        // 마크업율 = (판매가 - 원가) / 원가 × 100
        BigDecimal markupRate = totalCost > 0
            ? BigDecimal.valueOf(grossProfit)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalCost), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        return new MenuCostAnalysis(
            menu.getId(),
            menu.getName(),
            totalCost,
            sellingPrice,
            grossProfit,
            marginRate,
            markupRate,
            ingredientCosts
        );
    }
}
