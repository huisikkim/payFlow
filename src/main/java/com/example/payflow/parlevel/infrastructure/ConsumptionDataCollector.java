package com.example.payflow.parlevel.infrastructure;

import com.example.payflow.parlevel.application.ConsumptionAnalysisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConsumptionDataCollector {
    
    private final ConsumptionAnalysisService consumptionAnalysisService;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(topics = "IngredientOrderConfirmed", groupId = "parlevel-group")
    public void handleIngredientOrderConfirmed(String message) {
        try {
            log.info("🎧 [Kafka] IngredientOrderConfirmed 이벤트 수신: {}", message);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            
            String storeId = (String) event.get("storeId");
            String orderId = (String) event.get("orderId");
            
            // 발주 확인 시 소비 예측 데이터로 간주
            // 실제로는 발주 품목 정보를 조회해야 하지만, 여기서는 간단히 로그만 남김
            log.info("📊 소비 데이터 수집 대상: storeId={}, orderId={}", storeId, orderId);
            
            // TODO: 발주 품목 정보를 조회하여 소비 패턴 기록
            // 현재는 초기 데이터로 충분하므로 추후 구현
            
        } catch (Exception e) {
            log.error("❌ 소비 데이터 수집 실패: {}", e.getMessage());
        }
    }
}
