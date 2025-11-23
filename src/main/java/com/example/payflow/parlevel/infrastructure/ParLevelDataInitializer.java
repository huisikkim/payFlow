package com.example.payflow.parlevel.infrastructure;

import com.example.payflow.parlevel.domain.ConsumptionPattern;
import com.example.payflow.parlevel.domain.ConsumptionPatternRepository;
import com.example.payflow.parlevel.domain.ParLevel;
import com.example.payflow.parlevel.domain.ParLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Random;

@Component
@Order(6)
@RequiredArgsConstructor
@Slf4j
public class ParLevelDataInitializer implements CommandLineRunner {
    
    private final ParLevelRepository parLevelRepository;
    private final ConsumptionPatternRepository consumptionRepository;
    
    @Override
    public void run(String... args) {
        if (parLevelRepository.count() > 0) {
            log.info("Par Level 데이터가 이미 존재합니다.");
            return;
        }
        
        log.info("🔄 Par Level 초기 데이터 생성 시작...");
        
        // Par Level 설정
        createParLevel("STORE_001", "양파", "kg", 50, 150, 30, 2, true);
        createParLevel("STORE_001", "당근", "kg", 40, 120, 25, 2, true);
        createParLevel("STORE_001", "감자", "kg", 60, 180, 35, 2, true);
        createParLevel("STORE_001", "대파", "kg", 20, 60, 15, 1, true);
        createParLevel("STORE_001", "마늘", "kg", 15, 45, 10, 2, true);
        
        // 소비 패턴 생성 (최근 30일)
        createConsumptionPatterns("STORE_001", "양파", "kg", 20, 30);
        createConsumptionPatterns("STORE_001", "당근", "kg", 15, 25);
        createConsumptionPatterns("STORE_001", "감자", "kg", 25, 35);
        createConsumptionPatterns("STORE_001", "대파", "kg", 8, 15);
        createConsumptionPatterns("STORE_001", "마늘", "kg", 5, 10);
        
        log.info("✅ Par Level 초기 데이터 생성 완료");
    }
    
    private void createParLevel(String storeId, String itemName, String unit,
                               Integer minLevel, Integer maxLevel, Integer safetyStock,
                               Integer leadTimeDays, Boolean autoOrderEnabled) {
        ParLevel parLevel = new ParLevel(
            storeId, itemName, unit, minLevel, maxLevel, safetyStock, leadTimeDays, autoOrderEnabled
        );
        parLevelRepository.save(parLevel);
        log.info("✅ Par Level 생성: itemName={}, min={}, max={}", itemName, minLevel, maxLevel);
    }
    
    private void createConsumptionPatterns(String storeId, String itemName, String unit,
                                          int minQuantity, int maxQuantity) {
        Random random = new Random();
        
        for (int i = 30; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            int quantity = minQuantity + random.nextInt(maxQuantity - minQuantity + 1);
            
            // 주말에는 소비량 20% 증가
            if (date.getDayOfWeek().getValue() >= 6) {
                quantity = (int) (quantity * 1.2);
            }
            
            ConsumptionPattern pattern = new ConsumptionPattern(
                storeId, itemName, date, quantity, unit
            );
            consumptionRepository.save(pattern);
        }
        
        log.info("✅ 소비 패턴 생성: itemName={}, 30일 데이터", itemName);
    }
    
    private void createInventory(String itemName, Integer quantity) {
        // 재고 데이터는 별도 시스템에서 관리되므로 여기서는 생성하지 않음
        // 테스트를 위해서는 수동으로 재고 데이터를 생성해야 함
    }
}
