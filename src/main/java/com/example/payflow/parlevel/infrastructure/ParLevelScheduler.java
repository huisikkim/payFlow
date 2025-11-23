package com.example.payflow.parlevel.infrastructure;

import com.example.payflow.parlevel.application.OrderPredictionService;
import com.example.payflow.parlevel.domain.ParLevel;
import com.example.payflow.parlevel.domain.ParLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ParLevelScheduler {
    
    private final ParLevelRepository parLevelRepository;
    private final OrderPredictionService orderPredictionService;
    
    // 매일 오전 6시에 실행
    @Scheduled(cron = "0 0 6 * * *")
    public void checkParLevelsAndGeneratePredictions() {
        log.info("🔄 Par Level 체크 시작...");
        
        // 모든 매장 조회
        List<ParLevel> allParLevels = parLevelRepository.findAll();
        Set<String> storeIds = allParLevels.stream()
            .map(ParLevel::getStoreId)
            .collect(Collectors.toSet());
        
        for (String storeId : storeIds) {
            try {
                orderPredictionService.generatePredictions(storeId);
                log.info("✅ Par Level 체크 완료: storeId={}", storeId);
            } catch (Exception e) {
                log.error("❌ Par Level 체크 실패: storeId={}, error={}", storeId, e.getMessage());
            }
        }
        
        log.info("🔄 Par Level 체크 종료");
    }
}
