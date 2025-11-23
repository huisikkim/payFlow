package com.example.payflow.pricelearning.infrastructure;

import com.example.payflow.ingredientorder.domain.IngredientOrder;
import com.example.payflow.ingredientorder.domain.IngredientOrderRepository;
import com.example.payflow.pricelearning.application.PriceAlertService;
import com.example.payflow.pricelearning.application.PriceLearningService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PriceHistoryCollector {
    
    private final PriceLearningService priceLearningService;
    private final PriceAlertService priceAlertService;
    private final IngredientOrderRepository orderRepository;
    private final Gson gson = new Gson();
    
    @KafkaListener(topics = "IngredientOrderCreated", groupId = "price-learning-group")
    @Transactional
    public void handleOrderCreated(String message) {
        try {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String orderId = json.get("orderId").getAsString();
            String distributorId = json.get("distributorId").getAsString();
            String storeId = json.get("storeId").getAsString();
            
            log.info("📊 발주 생성 이벤트 수신: orderId={}", orderId);
            
            // 발주 조회
            IngredientOrder order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("발주를 찾을 수 없습니다: " + orderId));
            
            // 각 품목의 단가 이력 저장 및 급등 체크
            order.getItems().forEach(item -> {
                // 단가 이력 저장
                priceLearningService.recordPrice(
                    item.getItemName(),
                    item.getUnitPrice(),
                    item.getUnit(),
                    orderId,
                    distributorId,
                    storeId
                );
                
                // 급등 체크
                priceAlertService.checkAndCreateAlert(
                    item.getItemName(),
                    item.getUnitPrice(),
                    orderId,
                    distributorId
                );
            });
            
            log.info("✅ 단가 학습 및 급등 체크 완료: orderId={}", orderId);
            
        } catch (Exception e) {
            log.error("단가 학습 처리 실패: error={}", e.getMessage(), e);
        }
    }
}
