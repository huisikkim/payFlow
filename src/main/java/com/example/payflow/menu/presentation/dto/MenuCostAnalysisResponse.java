package com.example.payflow.menu.presentation.dto;

import com.example.payflow.menu.domain.MenuCostAnalysis;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class MenuCostAnalysisResponse {
    private Long menuId;
    private String menuName;
    private Long totalCost;
    private Long sellingPrice;
    private Long grossProfit;
    private BigDecimal marginRate;
    private BigDecimal markupRate;
    private List<IngredientCostResponse> ingredientCosts;
    
    @Getter
    @AllArgsConstructor
    public static class IngredientCostResponse {
        private String ingredientName;
        private BigDecimal quantity;
        private String unit;
        private Long unitPrice;
        private Long cost;
        private BigDecimal costRatio;
        
        public static IngredientCostResponse from(MenuCostAnalysis.IngredientCost cost) {
            return new IngredientCostResponse(
                cost.getIngredientName(),
                cost.getQuantity(),
                cost.getUnit(),
                cost.getUnitPrice(),
                cost.getCost(),
                cost.getCostRatio()
            );
        }
    }
    
    public static MenuCostAnalysisResponse from(MenuCostAnalysis analysis) {
        List<IngredientCostResponse> costs = analysis.getIngredientCosts().stream()
            .map(IngredientCostResponse::from)
            .toList();
        
        return new MenuCostAnalysisResponse(
            analysis.getMenuId(),
            analysis.getMenuName(),
            analysis.getTotalCost(),
            analysis.getSellingPrice(),
            analysis.getGrossProfit(),
            analysis.getMarginRate(),
            analysis.getMarkupRate(),
            costs
        );
    }
}
