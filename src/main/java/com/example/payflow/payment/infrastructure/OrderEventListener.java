package com.example.payflow.payment.infrastructure;

import com.example.payflow.payment.application.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {
    
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(topics = "OrderCreated", groupId = "payment-service")
    public void handleOrderCreated(String message) {
        try {
            log.info("📥 주문 생성 이벤트 수신: {}", message);
            
            JsonNode event = objectMapper.readTree(message);
            String orderId = event.get("orderId").asText();
            String orderName = event.get("orderName").asText();
            Long amount = event.get("amount").asLong();
            String customerEmail = event.get("customerEmail").asText();
            
            // 결제 정보 생성
            paymentService.createPayment(orderId, orderName, amount, customerEmail);
            
            log.info("✅ 결제 정보 생성 완료: orderId={}", orderId);
        } catch (Exception e) {
            log.error("❌ 주문 생성 이벤트 처리 실패", e);
        }
    }
}
