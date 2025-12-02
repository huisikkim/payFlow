package com.example.payflow.menu.infrastructure;

import com.example.payflow.menu.domain.Menu;
import com.example.payflow.menu.domain.MenuRepository;
import com.example.payflow.menu.domain.RecipeIngredient;
import com.example.payflow.pricelearning.application.PriceLearningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Order(100)
@RequiredArgsConstructor
@Slf4j
public class MenuDataInitializer implements CommandLineRunner {
    
    private final MenuRepository menuRepository;
    private final PriceLearningService priceLearningService;
    
    @Override
    public void run(String... args) {
        if (menuRepository.count() > 0) {
            log.info("🍽️ 메뉴 데이터가 이미 존재합니다.");
            return;
        }
        
        log.info("🍽️ 메뉴 초기 데이터 생성 시작...");
        
        // 단가 데이터는 실제 발주 시 자동 수집되므로 초기 데이터 생성 제거
        // createPriceData();
        
        // 메뉴 생성
        createKimchiJjigae();
        createDoenjangJjigae();
        createBulgogi();
        createBibimbap();
        
        log.info("🍽️ 메뉴 초기 데이터 생성 완료!");
    }
    
    private void createPriceData() {
        // 김치찌개 재료
        priceLearningService.recordPrice("김치", 3000L, "kg", "ORDER-001", "DIST-001", "STORE-001");
        priceLearningService.recordPrice("돼지고기", 8000L, "kg", "ORDER-001", "DIST-001", "STORE-001");
        priceLearningService.recordPrice("두부", 1500L, "모", "ORDER-001", "DIST-001", "STORE-001");
        priceLearningService.recordPrice("대파", 2000L, "kg", "ORDER-001", "DIST-001", "STORE-001");
        priceLearningService.recordPrice("고춧가루", 15000L, "kg", "ORDER-001", "DIST-001", "STORE-001");
        
        // 된장찌개 재료
        priceLearningService.recordPrice("된장", 8000L, "kg", "ORDER-002", "DIST-001", "STORE-001");
        priceLearningService.recordPrice("감자", 2000L, "kg", "ORDER-002", "DIST-001", "STORE-001");
        priceLearningService.recordPrice("애호박", 3000L, "kg", "ORDER-002", "DIST-001", "STORE-001");
        priceLearningService.recordPrice("양파", 1500L, "kg", "ORDER-002", "DIST-001", "STORE-001");
        
        // 불고기 재료
        priceLearningService.recordPrice("소고기", 25000L, "kg", "ORDER-003", "DIST-001", "STORE-001");
        priceLearningService.recordPrice("간장", 5000L, "L", "ORDER-003", "DIST-001", "STORE-001");
        priceLearningService.recordPrice("설탕", 2000L, "kg", "ORDER-003", "DIST-001", "STORE-001");
        priceLearningService.recordPrice("참기름", 12000L, "L", "ORDER-003", "DIST-001", "STORE-001");
        priceLearningService.recordPrice("마늘", 8000L, "kg", "ORDER-003", "DIST-001", "STORE-001");
        
        // 비빔밥 재료
        priceLearningService.recordPrice("쌀", 3000L, "kg", "ORDER-004", "DIST-001", "STORE-001");
        priceLearningService.recordPrice("시금치", 4000L, "kg", "ORDER-004", "DIST-001", "STORE-001");
        priceLearningService.recordPrice("콩나물", 2000L, "kg", "ORDER-004", "DIST-001", "STORE-001");
        priceLearningService.recordPrice("고사리", 15000L, "kg", "ORDER-004", "DIST-001", "STORE-001");
        priceLearningService.recordPrice("계란", 200L, "개", "ORDER-004", "DIST-001", "STORE-001");
        priceLearningService.recordPrice("고추장", 6000L, "kg", "ORDER-004", "DIST-001", "STORE-001");
        
        log.info("💰 단가 데이터 생성 완료");
    }
    
    private void createKimchiJjigae() {
        Menu menu = new Menu(
            "김치찌개",
            "묵은지와 돼지고기로 끓인 얼큰한 김치찌개",
            "한식",
            "STORE-001",
            8000L
        );
        
        menu.addRecipeIngredient(new RecipeIngredient("김치", new BigDecimal("0.3"), "kg", "묵은지 사용"));
        menu.addRecipeIngredient(new RecipeIngredient("돼지고기", new BigDecimal("0.15"), "kg", "삼겹살"));
        menu.addRecipeIngredient(new RecipeIngredient("두부", new BigDecimal("0.5"), "모", ""));
        menu.addRecipeIngredient(new RecipeIngredient("대파", new BigDecimal("0.05"), "kg", ""));
        menu.addRecipeIngredient(new RecipeIngredient("고춧가루", new BigDecimal("0.01"), "kg", ""));
        
        menuRepository.save(menu);
        log.info("✅ 김치찌개 메뉴 생성");
    }
    
    private void createDoenjangJjigae() {
        Menu menu = new Menu(
            "된장찌개",
            "구수한 된장과 신선한 채소로 끓인 된장찌개",
            "한식",
            "STORE-001",
            7000L
        );
        
        menu.addRecipeIngredient(new RecipeIngredient("된장", new BigDecimal("0.05"), "kg", ""));
        menu.addRecipeIngredient(new RecipeIngredient("두부", new BigDecimal("0.5"), "모", ""));
        menu.addRecipeIngredient(new RecipeIngredient("감자", new BigDecimal("0.1"), "kg", ""));
        menu.addRecipeIngredient(new RecipeIngredient("애호박", new BigDecimal("0.1"), "kg", ""));
        menu.addRecipeIngredient(new RecipeIngredient("양파", new BigDecimal("0.05"), "kg", ""));
        menu.addRecipeIngredient(new RecipeIngredient("대파", new BigDecimal("0.03"), "kg", ""));
        
        menuRepository.save(menu);
        log.info("✅ 된장찌개 메뉴 생성");
    }
    
    private void createBulgogi() {
        Menu menu = new Menu(
            "불고기",
            "달콤한 양념에 재운 한우 불고기",
            "한식",
            "STORE-001",
            15000L
        );
        
        menu.addRecipeIngredient(new RecipeIngredient("소고기", new BigDecimal("0.2"), "kg", "한우 등심"));
        menu.addRecipeIngredient(new RecipeIngredient("양파", new BigDecimal("0.1"), "kg", ""));
        menu.addRecipeIngredient(new RecipeIngredient("대파", new BigDecimal("0.05"), "kg", ""));
        menu.addRecipeIngredient(new RecipeIngredient("간장", new BigDecimal("0.03"), "L", ""));
        menu.addRecipeIngredient(new RecipeIngredient("설탕", new BigDecimal("0.02"), "kg", ""));
        menu.addRecipeIngredient(new RecipeIngredient("참기름", new BigDecimal("0.01"), "L", ""));
        menu.addRecipeIngredient(new RecipeIngredient("마늘", new BigDecimal("0.01"), "kg", ""));
        
        menuRepository.save(menu);
        log.info("✅ 불고기 메뉴 생성");
    }
    
    private void createBibimbap() {
        Menu menu = new Menu(
            "비빔밥",
            "다양한 나물과 고추장을 비벼 먹는 건강한 비빔밥",
            "한식",
            "STORE-001",
            9000L
        );
        
        menu.addRecipeIngredient(new RecipeIngredient("쌀", new BigDecimal("0.15"), "kg", ""));
        menu.addRecipeIngredient(new RecipeIngredient("소고기", new BigDecimal("0.05"), "kg", "다진 소고기"));
        menu.addRecipeIngredient(new RecipeIngredient("시금치", new BigDecimal("0.05"), "kg", ""));
        menu.addRecipeIngredient(new RecipeIngredient("콩나물", new BigDecimal("0.05"), "kg", ""));
        menu.addRecipeIngredient(new RecipeIngredient("고사리", new BigDecimal("0.03"), "kg", ""));
        menu.addRecipeIngredient(new RecipeIngredient("애호박", new BigDecimal("0.05"), "kg", ""));
        menu.addRecipeIngredient(new RecipeIngredient("당근", new BigDecimal("0.03"), "kg", ""));
        menu.addRecipeIngredient(new RecipeIngredient("계란", new BigDecimal("1"), "개", ""));
        menu.addRecipeIngredient(new RecipeIngredient("고추장", new BigDecimal("0.03"), "kg", ""));
        menu.addRecipeIngredient(new RecipeIngredient("참기름", new BigDecimal("0.01"), "L", ""));
        
        menuRepository.save(menu);
        log.info("✅ 비빔밥 메뉴 생성");
    }
}
