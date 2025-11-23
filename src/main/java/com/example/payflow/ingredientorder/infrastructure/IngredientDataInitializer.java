package com.example.payflow.ingredientorder.infrastructure;

import com.example.payflow.distributor.application.DistributorService;
import com.example.payflow.store.application.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class IngredientDataInitializer implements CommandLineRunner {
    
    private final StoreService storeService;
    private final DistributorService distributorService;
    
    @Override
    public void run(String... args) {
        try {
            initializeTestData();
        } catch (Exception e) {
            log.info("테스트 데이터가 이미 존재하거나 초기화 중 오류 발생: {}", e.getMessage());
        }
    }
    
    private void initializeTestData() {
        log.info("🌱 식자재 발주 플랫폼 테스트 데이터 초기화 시작");
        
        // 매장 생성
        try {
            storeService.createStore("STORE_001", "맛있는 식당", "김사장", "010-1234-5678", "서울시 강남구");
            storeService.createStore("STORE_002", "행복한 카페", "이사장", "010-2345-6789", "서울시 서초구");
            storeService.createStore("STORE_TEST", "테스트 매장", "테스트", "010-0000-0000", "테스트 주소");
        } catch (Exception e) {
            log.debug("매장 데이터 초기화 스킵");
        }
        
        // 유통사 생성
        try {
            distributorService.createDistributor("DIST_001", "신선식자재", "123-45-67890", "02-1234-5678", "박매니저");
            distributorService.createDistributor("DIST_002", "프리미엄푸드", "234-56-78901", "02-2345-6789", "최매니저");
            distributorService.createDistributor("DIST_TEST", "테스트 유통사", "000-00-00000", "02-0000-0000", "테스트");
        } catch (Exception e) {
            log.debug("유통사 데이터 초기화 스킵");
        }
        
        log.info("✅ 식자재 발주 플랫폼 테스트 데이터 초기화 완료");
    }
}
