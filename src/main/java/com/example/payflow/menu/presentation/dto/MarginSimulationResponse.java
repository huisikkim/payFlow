package com.example.payflow.menu.presentation.dto;

import com.example.payflow.menu.domain.MarginSimulation;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class MarginSimulationResponse {
    private Long menuId;
    private String menuName;
    private Long totalCost;
    private Long currentSellingPrice;
    private BigDecimal currentMarginRate;
    private Long targetSellingPrice;
    private BigDecimal targetMarginRate;
    private Long priceDifference;
    private BigDecimal marginDifference;
    
    public static MarginSimulationResponse from(MarginSimulation simulation) {
        return new MarginSimulationResponse(
            simulation.getMenuId(),
            simulation.getMenuName(),
            simulation.getTotalCost(),
            simulation.getCurrentSellingPrice(),
            simulation.getCurrentMarginRate(),
            simulation.getTargetSellingPrice(),
            simulation.getTargetMarginRate(),
            simulation.getPriceDifference(),
            simulation.getMarginDifference()
        );
    }
}
