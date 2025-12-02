package com.example.payflow.pricelearning.infrastructure;

import com.example.payflow.catalog.domain.DistributorOrder;
import com.example.payflow.catalog.domain.DistributorOrderRepository;
import com.example.payflow.pricelearning.application.PriceAlertService;
import com.example.payflow.pricelearning.application.PriceLearningService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 카탈로그 주문 결제 완료 시 가격 이력 수집
 * - 가게 사장님이 카탈로그에서 주문하면 가격 학습 시스템에 데이터 저장
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CatalogOrderPriceCollector {
    
    private final PriceLearningService priceLearningService;
    private final PriceAlertService priceAlertService;
    private final DistributorOrderRepository orderRepository;
    private final Gson gson = new Gson();
    
    @KafkaListener(topics = "CatalogOrderPaymentCompleted", groupId = "price-learning-catalog-group")
    @Transactional
    public void handleCatalogOrderCompleted(String message) {
        try {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String orderNumber = json.get("orderNumber").getAsString();
            String storeId = json.get("storeId").getAsString();
            String distributorId = json.get("distributorId").getAsString();
            
            log.info("📊 카탈로그 주문 결제 완료 이벤트 수신: orderNumber={}", orderNumber);
            
            // 주문 조회
            DistributorOrder order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderNumber));
            
            // 각 품목의 단가 이력 저장 및 급등 체크
            order.getItems().forEach(item -> {
                // 단가 이력 저장
                priceLearningService.recordPrice(
                    item.getProductName(),
                    item.getUnitPrice(),
                    item.getUnit(),
                    orderNumber,
                    distributorId,
                    storeId
                );
                
                // 급등 체크
                priceAlertService.checkAndCreateAlert(
                    item.getProductName(),
                    item.getUnitPrice(),
                    orderNumber,
                    distributorId
                );
            });
            
            log.info("✅ 카탈로그 주문 단가 학습 완료: orderNumber={}, itemCount={}", 
                orderNumber, order.getItems().size());
            
        } catch (Exception e) {
            log.error("카탈로그 주문 단가 학습 실패: error={}", e.getMessage(), e);
        }
    }
}
