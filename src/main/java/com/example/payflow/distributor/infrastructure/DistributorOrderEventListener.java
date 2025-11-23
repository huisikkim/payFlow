package com.example.payflow.distributor.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DistributorOrderEventListener {
    
    private final ObjectMapper objectMapper;
    
    @KafkaListener(topics = "IngredientOrderConfirmed", groupId = "distributor-order-group")
    public void handleOrderConfirmed(String message) {
        try {
            log.info("🎧 [Kafka] IngredientOrderConfirmed 이벤트 수신: {}", message);
            // 정산 프로세스 트리거 등 추가 처리
        } catch (Exception e) {
            log.error("❌ IngredientOrderConfirmed 이벤트 처리 실패", e);
        }
    }
    
    @KafkaListener(topics = "IngredientOrderRejected", groupId = "distributor-order-group")
    public void handleOrderRejected(String message) {
        try {
            log.info("🎧 [Kafka] IngredientOrderRejected 이벤트 수신: {}", message);
            // 매장 알림 등 추가 처리
        } catch (Exception e) {
            log.error("❌ IngredientOrderRejected 이벤트 처리 실패", e);
        }
    }
}
