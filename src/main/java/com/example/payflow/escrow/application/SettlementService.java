package com.example.payflow.escrow.application;

import com.example.payflow.common.event.EventPublisher;
import com.example.payflow.escrow.application.dto.SettlementResponse;
import com.example.payflow.escrow.domain.*;
import com.example.payflow.escrow.domain.event.EscrowCompletedEvent;
import com.example.payflow.escrow.domain.event.SettlementFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {
    
    private final EscrowTransactionRepository escrowTransactionRepository;
    private final SettlementRepository settlementRepository;
    private final EventPublisher eventPublisher;
    private final EscrowEventSourcingService eventSourcingService;
    
    @Transactional
    public SettlementResponse startSettlement(String transactionId) {
        log.info("정산 시작: transactionId={}", transactionId);
        
        EscrowTransaction transaction = escrowTransactionRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new EscrowNotFoundException(transactionId));
        
        // 이미 정산이 존재하는지 확인
        if (settlementRepository.existsByTransactionId(transactionId)) {
            throw new IllegalStateException("이미 정산이 진행 중이거나 완료되었습니다.");
        }
        
        // 거래 상태 업데이트
        transaction.startSettlement();
        
        // 정산 정보 생성
        BigDecimal totalAmount = transaction.getAmount();
        BigDecimal feeAmount = transaction.calculateFee();
        BigDecimal sellerAmount = transaction.calculateSellerAmount();
        
        Settlement settlement = new Settlement(
            transactionId,
            totalAmount,
            feeAmount,
            sellerAmount,
            transaction.getSeller().getUserId()
        );
        settlementRepository.save(settlement);
        
        // 이벤트 소싱에 기록
        eventSourcingService.storeEscrowEvent(
            transactionId,
            "SettlementStarted",
            "OWNERSHIP_TRANSFERRED",
            "SETTLING",
            Map.of(
                "totalAmount", totalAmount,
                "feeAmount", feeAmount,
                "sellerAmount", sellerAmount,
                "settlementId", settlement.getId()
            ),
            "SYSTEM"
        );
        
        log.info("정산 시작 완료: transactionId={}, settlementId={}", transactionId, settlement.getId());
        
        return SettlementResponse.from(settlement);
    }
    
    @Transactional
    public SettlementResponse completeSettlement(String transactionId, String paymentMethod, String paymentReference) {
        log.info("정산 완료 처리: transactionId={}", transactionId);
        
        EscrowTransaction transaction = escrowTransactionRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new EscrowNotFoundException(transactionId));
        
        Settlement settlement = settlementRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new IllegalStateException("정산 정보를 찾을 수 없습니다."));
        
        // 정산 완료
        settlement.complete(paymentMethod, paymentReference);
        
        // 거래 상태 업데이트
        transaction.completeSettlement();
        
        // 이벤트 소싱에 기록
        eventSourcingService.storeEscrowEvent(
            transactionId,
            "EscrowCompleted",
            "SETTLING",
            "COMPLETED",
            Map.of(
                "sellerId", transaction.getSeller().getUserId(),
                "sellerAmount", settlement.getSellerAmount(),
                "feeAmount", settlement.getFeeAmount(),
                "paymentMethod", paymentMethod
            ),
            "SYSTEM"
        );
        
        // 이벤트 발행
        EscrowCompletedEvent event = new EscrowCompletedEvent(
            transaction.getTransactionId(),
            transaction.getSeller().getUserId(),
            settlement.getSellerAmount(),
            settlement.getFeeAmount()
        );
        eventPublisher.publish(event);
        
        log.info("정산 완료: transactionId={}, settlementId={}", transactionId, settlement.getId());
        
        return SettlementResponse.from(settlement);
    }
    
    @Transactional
    public void handleSettlementFailure(String transactionId, String reason) {
        log.error("정산 실패 처리: transactionId={}, reason={}", transactionId, reason);
        
        EscrowTransaction transaction = escrowTransactionRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new EscrowNotFoundException(transactionId));
        
        Settlement settlement = settlementRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new IllegalStateException("정산 정보를 찾을 수 없습니다."));
        
        // 정산 실패 처리
        settlement.markFailed(reason);
        
        // 거래 상태 업데이트
        transaction.markSettlementFailed();
        
        // 이벤트 소싱에 기록
        eventSourcingService.storeEscrowEvent(
            transactionId,
            "SettlementFailed",
            "SETTLING",
            "SETTLEMENT_FAILED",
            Map.of("reason", reason),
            "SYSTEM"
        );
        
        // 이벤트 발행
        SettlementFailedEvent event = new SettlementFailedEvent(
            transaction.getTransactionId(),
            reason
        );
        eventPublisher.publish(event);
        
        log.error("정산 실패 처리 완료: transactionId={}", transactionId);
    }
    
    @Transactional(readOnly = true)
    public SettlementResponse getSettlementByTransaction(String transactionId) {
        Settlement settlement = settlementRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new IllegalStateException("정산 정보를 찾을 수 없습니다."));
        
        return SettlementResponse.from(settlement);
    }
}
