package com.example.payflow.ingredientorder.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IngredientOrderEventListener {
    
    private final ObjectMapper objectMapper;
    
    @KafkaListener(topics = "IngredientOrderCreated", groupId = "ingredient-order-group")
    public void handleOrderCreated(String message) {
        try {
            log.info("🎧 [Kafka] IngredientOrderCreated 이벤트 수신: {}", message);
            // 추가 처리 로직 (알림, 통계 등)
        } catch (Exception e) {
            log.error("❌ IngredientOrderCreated 이벤트 처리 실패", e);
        }
    }
}
