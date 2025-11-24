package com.example.payflow.menu.application;

import com.example.payflow.menu.domain.*;
import com.example.payflow.pricelearning.application.PriceLearningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuCostCalculator {
    
    private final MenuRepository menuRepository;
    private final PriceLearningService priceLearningService;
    
    @Transactional(readOnly = true)
    public MenuCostAnalysis calculateCost(Long menuId) {
        Menu menu = menuRepository.findByIdWithIngredients(menuId);
        if (menu == null) {
            throw new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + menuId);
        }
        
        List<MenuCostAnalysis.IngredientCost> ingredientCosts = new ArrayList<>();
        Long totalCost = 0L;
        
        for (RecipeIngredient ingredient : menu.getRecipeIngredients()) {
            // 단가 학습 시스템에서 추천 단가 조회
            Long unitPrice = priceLearningService.calculateRecommendedPrice(ingredient.getIngredientName());
            
            if (unitPrice == 0L) {
                log.warn("⚠️ 단가 정보 없음: ingredientName={}", ingredient.getIngredientName());
            }
            
            // 원가 계산 (사용량 × 단가)
            Long cost = ingredient.getQuantity()
                .multiply(BigDecimal.valueOf(unitPrice))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
            
            totalCost += cost;
            
            ingredientCosts.add(new MenuCostAnalysis.IngredientCost(
                ingredient.getIngredientName(),
                ingredient.getQuantity(),
                ingredient.getUnit(),
                unitPrice,
                cost,
                BigDecimal.ZERO // 비율은 나중에 계산
            ));
        }
        
        // 각 재료의 원가 비율 계산
        final Long finalTotalCost = totalCost;
        ingredientCosts = ingredientCosts.stream()
            .map(ic -> {
                BigDecimal ratio = finalTotalCost > 0
                    ? BigDecimal.valueOf(ic.getCost())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(finalTotalCost), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
                
                return new MenuCostAnalysis.IngredientCost(
                    ic.getIngredientName(),
                    ic.getQuantity(),
                    ic.getUnit(),
                    ic.getUnitPrice(),
                    ic.getCost(),
                    ratio
                );
            })
            .toList();
        
        MenuCostAnalysis analysis = MenuCostAnalysis.calculate(menu, ingredientCosts);
        
        log.info("💰 메뉴 원가 계산: menuId={}, menuName={}, totalCost={}, sellingPrice={}, marginRate={}%",
            menuId, menu.getName(), totalCost, menu.getSellingPrice(), analysis.getMarginRate());
        
        return analysis;
    }
    
    @Transactional(readOnly = true)
    public List<MenuCostAnalysis> calculateAllCosts(String storeId) {
        List<Menu> menus = menuRepository.findByStoreId(storeId);
        
        return menus.stream()
            .map(menu -> calculateCost(menu.getId()))
            .toList();
    }
}
