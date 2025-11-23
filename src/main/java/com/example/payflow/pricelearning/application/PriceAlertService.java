package com.example.payflow.pricelearning.application;

import com.example.payflow.common.event.EventPublisher;
import com.example.payflow.pricelearning.domain.*;
import com.example.payflow.pricelearning.domain.event.PriceSurgeAlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceAlertService {
    
    private final PriceAlertRepository alertRepository;
    private final ItemPriceHistoryRepository priceHistoryRepository;
    private final EventPublisher eventPublisher;
    
    private static final double MODERATE_SURGE_THRESHOLD = 20.0; // 20%
    private static final double HIGH_SURGE_THRESHOLD = 50.0;     // 50%
    private static final double EXTREME_SURGE_THRESHOLD = 100.0; // 100%
    
    @Transactional
    public PriceAlert checkAndCreateAlert(String itemName, Long currentPrice, 
                                         String orderId, String distributorId) {
        // 최근 30일 평균 단가 조회
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        Double avgPrice = priceHistoryRepository.calculateAveragePrice(itemName, thirtyDaysAgo);
        
        if (avgPrice == null || avgPrice == 0) {
            log.debug("평균 단가 데이터 없음: itemName={}", itemName);
            return null;
        }
        
        long averagePrice = Math.round(avgPrice);
        double surgePercentage = ((currentPrice - averagePrice) / (double) averagePrice) * 100;
        
        // 급등 여부 확인
        if (surgePercentage < MODERATE_SURGE_THRESHOLD) {
            log.debug("정상 단가 범위: itemName={}, surge={}%", itemName, String.format("%.1f", surgePercentage));
            return null;
        }
        
        // 급등 유형 결정
        PriceAlertType alertType = determineAlertType(surgePercentage);
        
        // 경고 생성
        String alertId = "ALERT_" + UUID.randomUUID().toString().substring(0, 8);
        PriceAlert alert = new PriceAlert(
            alertId, itemName, currentPrice, averagePrice,
            orderId, distributorId, alertType
        );
        
        alertRepository.save(alert);
        
        log.warn("🚨 단가 급등 감지! itemName={}, surge={}%, current={}, avg={}", 
            itemName, String.format("%.1f", surgePercentage), currentPrice, averagePrice);
        
        // Kafka 이벤트 발행
        PriceSurgeAlertEvent event = new PriceSurgeAlertEvent(
            alertId, itemName, currentPrice, averagePrice, surgePercentage, orderId
        );
        eventPublisher.publish(event);
        
        return alert;
    }
    
    private PriceAlertType determineAlertType(double surgePercentage) {
        if (surgePercentage >= EXTREME_SURGE_THRESHOLD) {
            return PriceAlertType.EXTREME_SURGE;
        } else if (surgePercentage >= HIGH_SURGE_THRESHOLD) {
            return PriceAlertType.HIGH_SURGE;
        } else {
            return PriceAlertType.MODERATE_SURGE;
        }
    }
    
    @Transactional(readOnly = true)
    public List<PriceAlert> getActiveAlerts() {
        return alertRepository.findByStatusOrderByCreatedAtDesc(PriceAlertStatus.ACTIVE);
    }
    
    @Transactional(readOnly = true)
    public List<PriceAlert> getRecentAlerts() {
        return alertRepository.findTop10ByOrderByCreatedAtDesc();
    }
    
    @Transactional(readOnly = true)
    public PriceAlert getAlert(String alertId) {
        return alertRepository.findByAlertId(alertId)
            .orElseThrow(() -> new IllegalArgumentException("경고를 찾을 수 없습니다: " + alertId));
    }
    
    @Transactional
    public void acknowledgeAlert(String alertId) {
        PriceAlert alert = getAlert(alertId);
        alert.acknowledge();
        log.info("경고 확인: alertId={}", alertId);
    }
    
    @Transactional
    public void resolveAlert(String alertId) {
        PriceAlert alert = getAlert(alertId);
        alert.resolve();
        log.info("경고 해결: alertId={}", alertId);
    }
}
