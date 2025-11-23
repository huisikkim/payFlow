package com.example.payflow.settlement.application;

import com.example.payflow.common.event.EventPublisher;
import com.example.payflow.logging.application.EventLoggingService;
import com.example.payflow.settlement.domain.IngredientSettlement;
import com.example.payflow.settlement.domain.IngredientSettlementRepository;
import com.example.payflow.settlement.domain.event.SettlementCompletedEvent;
import com.example.payflow.settlement.domain.event.SettlementCreatedEvent;
import com.example.payflow.settlement.presentation.dto.CompleteSettlementRequest;
import com.example.payflow.settlement.presentation.dto.SettlementResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngredientSettlementService {
    
    private final IngredientSettlementRepository settlementRepository;
    private final EventPublisher eventPublisher;
    private final EventLoggingService eventLoggingService;
    
    @Transactional
    public SettlementResponse createSettlement(String orderId, String storeId, 
                                              String distributorId, Long settlementAmount) {
        String settlementId = "SETTLE_" + UUID.randomUUID().toString().substring(0, 8);
        
        IngredientSettlement settlement = new IngredientSettlement(
            settlementId,
            storeId,
            distributorId,
            orderId,
            settlementAmount
        );
        
        settlementRepository.save(settlement);
        
        log.info("💰 정산 생성: settlementId={}, orderId={}, amount={}", 
            settlementId, orderId, settlementAmount);
        
        // Kafka 이벤트 발행
        SettlementCreatedEvent event = new SettlementCreatedEvent(
            settlementId,
            orderId,
            storeId,
            distributorId,
            settlementAmount
        );
        eventPublisher.publish(event);
        
        // 이벤트 로그
        eventLoggingService.logEvent(
            settlementId,
            "SettlementCreated",
            "settlement",
            Map.of(
                "settlementId", settlementId,
                "orderId", orderId,
                "storeId", storeId,
                "distributorId", distributorId,
                "settlementAmount", settlementAmount
            )
        );
        
        return SettlementResponse.from(settlement);
    }
    
    @Transactional
    public void completeSettlement(String settlementId, Long paidAmount) {
        IngredientSettlement settlement = settlementRepository.findBySettlementId(settlementId)
            .orElseThrow(() -> new IllegalArgumentException("정산을 찾을 수 없습니다: " + settlementId));
        
        settlement.complete(paidAmount);
        settlementRepository.save(settlement);
        
        log.info("✅ 정산 완료: settlementId={}, paidAmount={}, outstandingAmount={}", 
            settlementId, paidAmount, settlement.getOutstandingAmount());
        
        // Kafka 이벤트 발행
        SettlementCompletedEvent event = new SettlementCompletedEvent(
            settlementId,
            settlement.getOrderId(),
            settlement.getStoreId(),
            paidAmount,
            settlement.getOutstandingAmount()
        );
        eventPublisher.publish(event);
        
        // 이벤트 로그
        eventLoggingService.logEvent(
            settlementId,
            "SettlementCompleted",
            "settlement",
            Map.of(
                "settlementId", settlementId,
                "paidAmount", paidAmount,
                "outstandingAmount", settlement.getOutstandingAmount()
            )
        );
    }
    
    @Transactional(readOnly = true)
    public SettlementResponse getSettlement(String settlementId) {
        IngredientSettlement settlement = settlementRepository.findBySettlementId(settlementId)
            .orElseThrow(() -> new IllegalArgumentException("정산을 찾을 수 없습니다: " + settlementId));
        return SettlementResponse.from(settlement);
    }
    
    @Transactional(readOnly = true)
    public List<SettlementResponse> getSettlementsByStore(String storeId) {
        return settlementRepository.findByStoreId(storeId).stream()
            .map(SettlementResponse::from)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<SettlementResponse> getSettlementsByDistributor(String distributorId) {
        return settlementRepository.findByDistributorId(distributorId).stream()
            .map(SettlementResponse::from)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Long calculateTotalOutstanding(String storeId) {
        Long total = settlementRepository.calculateTotalOutstandingByStore(storeId);
        return total != null ? total : 0L;
    }
}
