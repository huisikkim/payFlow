package com.example.payflow.menu.application;

import com.example.payflow.menu.domain.MarginSimulation;
import com.example.payflow.menu.domain.MenuCostAnalysis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarginSimulator {
    
    private final MenuCostCalculator menuCostCalculator;
    
    public MarginSimulation simulateByPrice(Long menuId, Long targetPrice) {
        MenuCostAnalysis analysis = menuCostCalculator.calculateCost(menuId);
        MarginSimulation simulation = MarginSimulation.simulateByPrice(analysis, targetPrice);
        
        log.info("📊 가격 기반 시뮬레이션: menuId={}, targetPrice={}, targetMargin={}%",
            menuId, targetPrice, simulation.getTargetMarginRate());
        
        return simulation;
    }
    
    public MarginSimulation simulateByMargin(Long menuId, BigDecimal targetMargin) {
        MenuCostAnalysis analysis = menuCostCalculator.calculateCost(menuId);
        MarginSimulation simulation = MarginSimulation.simulateByMargin(analysis, targetMargin);
        
        log.info("📊 마진 기반 시뮬레이션: menuId={}, targetMargin={}%, targetPrice={}",
            menuId, targetMargin, simulation.getTargetSellingPrice());
        
        return simulation;
    }
}
