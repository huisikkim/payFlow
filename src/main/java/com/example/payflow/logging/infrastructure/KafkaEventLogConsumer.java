package com.example.payflow.logging.infrastructure;

import com.example.payflow.logging.application.EventLogRequest;
import com.example.payflow.logging.application.EventLoggingService;
import com.example.payflow.logging.domain.EventStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka 이벤트 로그 수집기
 * 모든 비즈니스 이벤트를 Kafka에서 수신하여 중앙 로그 저장소에 저장
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventLogConsumer {
    
    private final EventLoggingService eventLoggingService;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(topics = "OrderCreated", groupId = "event-log-collector")
    public void consumeOrderCreated(String message) {
        collectEvent("OrderCreated", "order", message);
    }
    
    @KafkaListener(topics = "PaymentApproved", groupId = "event-log-collector")
    public void consumePaymentApproved(String message) {
        collectEvent("PaymentApproved", "payment", message);
    }
    
    @KafkaListener(topics = "PaymentFailed", groupId = "event-log-collector")
    public void consumePaymentFailed(String message) {
        collectEvent("PaymentFailed", "payment", message);
    }
    
    @KafkaListener(topics = "StageStarted", groupId = "event-log-collector")
    public void consumeStageStarted(String message) {
        collectEvent("StageStarted", "stage", message);
    }
    
    @KafkaListener(topics = "SettlementCompleted", groupId = "event-log-collector")
    public void consumeSettlementCompleted(String message) {
        collectEvent("SettlementCompleted", "stage", message);
    }
    
    @KafkaListener(topics = "PaymentDue", groupId = "event-log-collector")
    public void consumePaymentDue(String message) {
        collectEvent("PaymentDue", "stage", message);
    }
    
    @KafkaListener(topics = "PayoutReady", groupId = "event-log-collector")
    public void consumePayoutReady(String message) {
        collectEvent("PayoutReady", "stage", message);
    }
    
    // 에스크로 이벤트
    @KafkaListener(topics = "EscrowCreated", groupId = "event-log-collector")
    public void consumeEscrowCreated(String message) {
        collectEvent("EscrowCreated", "escrow", message);
    }
    
    @KafkaListener(topics = "DepositConfirmed", groupId = "event-log-collector")
    public void consumeDepositConfirmed(String message) {
        collectEvent("DepositConfirmed", "escrow", message);
    }
    
    @KafkaListener(topics = "VehicleDelivered", groupId = "event-log-collector")
    public void consumeVehicleDelivered(String message) {
        collectEvent("VehicleDelivered", "escrow", message);
    }
    
    @KafkaListener(topics = "VehicleVerified", groupId = "event-log-collector")
    public void consumeVehicleVerified(String message) {
        collectEvent("VehicleVerified", "escrow", message);
    }
    
    @KafkaListener(topics = "VerificationFailed", groupId = "event-log-collector")
    public void consumeVerificationFailed(String message) {
        collectEvent("VerificationFailed", "escrow", message);
    }
    
    @KafkaListener(topics = "OwnershipTransferred", groupId = "event-log-collector")
    public void consumeOwnershipTransferred(String message) {
        collectEvent("OwnershipTransferred", "escrow", message);
    }
    
    @KafkaListener(topics = "EscrowCompleted", groupId = "event-log-collector")
    public void consumeEscrowCompleted(String message) {
        collectEvent("EscrowCompleted", "escrow", message);
    }
    
    @KafkaListener(topics = "EscrowCancelled", groupId = "event-log-collector")
    public void consumeEscrowCancelled(String message) {
        collectEvent("EscrowCancelled", "escrow", message);
    }
    
    @KafkaListener(topics = "SettlementFailed", groupId = "event-log-collector")
    public void consumeSettlementFailed(String message) {
        collectEvent("SettlementFailed", "escrow", message);
    }
    
    @KafkaListener(topics = "DisputeRaised", groupId = "event-log-collector")
    public void consumeDisputeRaised(String message) {
        collectEvent("DisputeRaised", "escrow", message);
    }
    
    @KafkaListener(topics = "DisputeResolved", groupId = "event-log-collector")
    public void consumeDisputeResolved(String message) {
        collectEvent("DisputeResolved", "escrow", message);
    }
    
    private void collectEvent(String eventType, String serviceName, String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            
            String correlationId = extractCorrelationId(jsonNode);
            String userId = extractUserId(jsonNode);
            
            eventLoggingService.logEvent(EventLogRequest.builder()
                .correlationId(correlationId)
                .eventType(eventType)
                .serviceName(serviceName)
                .payload(message)
                .userId(userId)
                .status(EventStatus.SUCCESS)
                .build());
            
            log.debug("Event collected: {} from {}", eventType, serviceName);
            
        } catch (Exception e) {
            log.error("Failed to collect event: {} - {}", eventType, e.getMessage());
            eventLoggingService.logFailedEvent(
                null, eventType, serviceName, e.getMessage()
            );
        }
    }
    
    private String extractCorrelationId(JsonNode jsonNode) {
        if (jsonNode.has("correlationId")) {
            return jsonNode.get("correlationId").asText();
        }
        if (jsonNode.has("orderId")) {
            return jsonNode.get("orderId").asText();
        }
        if (jsonNode.has("paymentId")) {
            return jsonNode.get("paymentId").asText();
        }
        if (jsonNode.has("transactionId")) {
            return jsonNode.get("transactionId").asText();
        }
        return null;
    }
    
    private String extractUserId(JsonNode jsonNode) {
        if (jsonNode.has("userId")) {
            return jsonNode.get("userId").asText();
        }
        if (jsonNode.has("username")) {
            return jsonNode.get("username").asText();
        }
        if (jsonNode.has("buyerId")) {
            return jsonNode.get("buyerId").asText();
        }
        if (jsonNode.has("sellerId")) {
            return jsonNode.get("sellerId").asText();
        }
        return null;
    }
}
