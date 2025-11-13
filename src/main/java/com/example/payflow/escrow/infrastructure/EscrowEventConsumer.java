package com.example.payflow.escrow.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 에스크로 이벤트 컨슈머
 * Kafka에서 에스크로 관련 이벤트를 구독하여 처리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EscrowEventConsumer {
    
    private final ObjectMapper objectMapper;
    
    @KafkaListener(topics = "EscrowCreated", groupId = "escrow-event-group")
    public void handleEscrowCreated(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            log.info("📨 에스크로 거래 생성 이벤트 수신: transactionId={}, buyerId={}, sellerId={}", 
                event.get("transactionId"), event.get("buyerId"), event.get("sellerId"));
            
            // 알림 처리 (향후 구현)
            // notificationService.notifyEscrowCreated(event);
            
        } catch (Exception e) {
            log.error("에스크로 생성 이벤트 처리 실패", e);
        }
    }
    
    @KafkaListener(topics = "DepositConfirmed", groupId = "escrow-event-group")
    public void handleDepositConfirmed(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            log.info("📨 입금 확인 이벤트 수신: transactionId={}, amount={}", 
                event.get("transactionId"), event.get("amount"));
            
            // 판매자에게 입금 확인 알림
            // notificationService.notifyDepositConfirmed(event);
            
        } catch (Exception e) {
            log.error("입금 확인 이벤트 처리 실패", e);
        }
    }
    
    @KafkaListener(topics = "VehicleDelivered", groupId = "escrow-event-group")
    public void handleVehicleDelivered(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            log.info("📨 차량 인도 이벤트 수신: transactionId={}, vehicleVin={}", 
                event.get("transactionId"), event.get("vehicleVin"));
            
            // 구매자에게 차량 인도 알림
            // notificationService.notifyVehicleDelivered(event);
            
        } catch (Exception e) {
            log.error("차량 인도 이벤트 처리 실패", e);
        }
    }
    
    @KafkaListener(topics = "VehicleVerified", groupId = "escrow-event-group")
    public void handleVehicleVerified(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            log.info("📨 차량 검증 완료 이벤트 수신: transactionId={}, verifiedBy={}", 
                event.get("transactionId"), event.get("verifiedBy"));
            
            // 구매자와 판매자에게 검증 완료 알림
            // notificationService.notifyVehicleVerified(event);
            
        } catch (Exception e) {
            log.error("차량 검증 이벤트 처리 실패", e);
        }
    }
    
    @KafkaListener(topics = "VerificationFailed", groupId = "escrow-event-group")
    public void handleVerificationFailed(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            log.warn("⚠️ 차량 검증 실패 이벤트 수신: transactionId={}, reason={}", 
                event.get("transactionId"), event.get("reason"));
            
            // 구매자와 판매자에게 검증 실패 알림
            // notificationService.notifyVerificationFailed(event);
            
        } catch (Exception e) {
            log.error("검증 실패 이벤트 처리 실패", e);
        }
    }
    
    @KafkaListener(topics = "OwnershipTransferred", groupId = "escrow-event-group")
    public void handleOwnershipTransferred(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            log.info("📨 명의 이전 완료 이벤트 수신: transactionId={}, newOwnerId={}", 
                event.get("transactionId"), event.get("newOwnerId"));
            
            // 구매자와 판매자에게 명의 이전 완료 알림
            // notificationService.notifyOwnershipTransferred(event);
            
            // 자동으로 정산 시작 트리거 (향후 구현)
            // settlementService.startSettlement(event.get("transactionId"));
            
        } catch (Exception e) {
            log.error("명의 이전 이벤트 처리 실패", e);
        }
    }
    
    @KafkaListener(topics = "EscrowCompleted", groupId = "escrow-event-group")
    public void handleEscrowCompleted(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            log.info("✅ 에스크로 거래 완료 이벤트 수신: transactionId={}, sellerId={}, sellerAmount={}", 
                event.get("transactionId"), event.get("sellerId"), event.get("sellerAmount"));
            
            // 판매자에게 정산 완료 알림
            // notificationService.notifyEscrowCompleted(event);
            
        } catch (Exception e) {
            log.error("거래 완료 이벤트 처리 실패", e);
        }
    }
    
    @KafkaListener(topics = "EscrowCancelled", groupId = "escrow-event-group")
    public void handleEscrowCancelled(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            log.info("❌ 에스크로 거래 취소 이벤트 수신: transactionId={}, reason={}, refundAmount={}", 
                event.get("transactionId"), event.get("reason"), event.get("refundAmount"));
            
            // 구매자와 판매자에게 취소 알림
            // notificationService.notifyEscrowCancelled(event);
            
        } catch (Exception e) {
            log.error("거래 취소 이벤트 처리 실패", e);
        }
    }
    
    @KafkaListener(topics = "SettlementFailed", groupId = "escrow-event-group")
    public void handleSettlementFailed(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            log.error("❌ 정산 실패 이벤트 수신: transactionId={}, reason={}", 
                event.get("transactionId"), event.get("reason"));
            
            // 관리자에게 정산 실패 알림
            // notificationService.notifyAdminSettlementFailed(event);
            
        } catch (Exception e) {
            log.error("정산 실패 이벤트 처리 실패", e);
        }
    }
    
    @KafkaListener(topics = "DisputeRaised", groupId = "escrow-event-group")
    public void handleDisputeRaised(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            log.warn("⚠️ 분쟁 제기 이벤트 수신: transactionId={}, raisedBy={}, reason={}", 
                event.get("transactionId"), event.get("raisedBy"), event.get("reason"));
            
            // 관리자에게 분쟁 제기 알림
            // notificationService.notifyAdminDisputeRaised(event);
            
            // 상대방에게 분쟁 제기 알림
            // notificationService.notifyCounterpartyDisputeRaised(event);
            
        } catch (Exception e) {
            log.error("분쟁 제기 이벤트 처리 실패", e);
        }
    }
    
    @KafkaListener(topics = "DisputeResolved", groupId = "escrow-event-group")
    public void handleDisputeResolved(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            log.info("✅ 분쟁 해결 이벤트 수신: transactionId={}, resolvedBy={}", 
                event.get("transactionId"), event.get("resolvedBy"));
            
            // 구매자와 판매자에게 분쟁 해결 알림
            // notificationService.notifyDisputeResolved(event);
            
        } catch (Exception e) {
            log.error("분쟁 해결 이벤트 처리 실패", e);
        }
    }
}
