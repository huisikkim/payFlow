package com.example.payflow.settlement.infrastructure;

import com.example.payflow.ingredientorder.domain.event.IngredientOrderConfirmedEvent;
import com.example.payflow.settlement.application.IngredientSettlementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IngredientSettlementEventListener {
    
    private final ObjectMapper objectMapper;
    private final IngredientSettlementService settlementService;
    
    @KafkaListener(topics = "IngredientOrderConfirmed", groupId = "settlement-group")
    public void handleOrderConfirmed(String message) {
        try {
            log.info("🎧 [Kafka] IngredientOrderConfirmed 이벤트 수신 (정산): {}", message);
            
            IngredientOrderConfirmedEvent event = objectMapper.readValue(message, IngredientOrderConfirmedEvent.class);
            
            // 정산 자동 생성
            settlementService.createSettlement(
                event.getOrderId(),
                event.getStoreId(),
                event.getDistributorId(),
                event.getTotalAmount()
            );
            
        } catch (Exception e) {
            log.error("❌ IngredientOrderConfirmed 이벤트 처리 실패 (정산)", e);
        }
    }
    
    @KafkaListener(topics = "SettlementCompleted", groupId = "settlement-group")
    public void handleSettlementCompleted(String message) {
        try {
            log.info("🎧 [Kafka] SettlementCompleted 이벤트 수신: {}", message);
            // 미수금 알림 등 추가 처리
        } catch (Exception e) {
            log.error("❌ SettlementCompleted 이벤트 처리 실패", e);
        }
    }
}
