package com.example.payflow.escrow.application;

import com.example.payflow.common.event.EventPublisher;
import com.example.payflow.escrow.application.dto.CreateEscrowRequest;
import com.example.payflow.escrow.application.dto.EscrowResponse;
import com.example.payflow.escrow.domain.*;
import com.example.payflow.escrow.domain.event.EscrowCancelledEvent;
import com.example.payflow.escrow.domain.event.EscrowCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EscrowService {
    
    private final EscrowTransactionRepository escrowTransactionRepository;
    private final EventPublisher eventPublisher;
    private final EscrowEventSourcingService eventSourcingService;
    
    @Transactional
    public EscrowResponse createEscrow(CreateEscrowRequest request) {
        log.info("에스크로 거래 생성 시작: buyer={}, seller={}", 
            request.getBuyer().getUserId(), request.getSeller().getUserId());
        
        Participant buyer = request.getBuyer().toEntity();
        Participant seller = request.getSeller().toEntity();
        Vehicle vehicle = request.getVehicle().toEntity();
        
        EscrowTransaction transaction;
        if (request.getFeeRate() != null) {
            transaction = new EscrowTransaction(buyer, seller, vehicle, request.getAmount(), request.getFeeRate());
        } else {
            transaction = new EscrowTransaction(buyer, seller, vehicle, request.getAmount());
        }
        
        escrowTransactionRepository.save(transaction);
        
        // 이벤트 소싱에 기록
        eventSourcingService.storeEscrowEvent(
            transaction.getTransactionId(),
            "EscrowCreated",
            null,
            "INITIATED",
            Map.of(
                "buyerId", buyer.getUserId(),
                "sellerId", seller.getUserId(),
                "amount", transaction.getAmount(),
                "vehicleVin", vehicle.getVin()
            ),
            buyer.getUserId()
        );
        
        // 이벤트 발행
        EscrowCreatedEvent event = new EscrowCreatedEvent(
            transaction.getTransactionId(),
            buyer.getUserId(),
            seller.getUserId(),
            transaction.getAmount(),
            vehicle.getVin()
        );
        eventPublisher.publish(event);
        
        log.info("에스크로 거래 생성 완료: transactionId={}", transaction.getTransactionId());
        
        return EscrowResponse.from(transaction);
    }
    
    @Transactional(readOnly = true)
    public EscrowResponse getEscrow(String transactionId) {
        EscrowTransaction transaction = escrowTransactionRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new EscrowNotFoundException(transactionId));
        
        return EscrowResponse.from(transaction);
    }
    
    @Transactional(readOnly = true)
    public List<EscrowResponse> getEscrowsByBuyer(String buyerId) {
        List<EscrowTransaction> transactions = escrowTransactionRepository.findByBuyerUserId(buyerId);
        return transactions.stream()
            .map(EscrowResponse::from)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<EscrowResponse> getEscrowsBySeller(String sellerId) {
        List<EscrowTransaction> transactions = escrowTransactionRepository.findBySellerUserId(sellerId);
        return transactions.stream()
            .map(EscrowResponse::from)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public EscrowResponse cancelEscrow(String transactionId, String reason) {
        log.info("에스크로 거래 취소 시작: transactionId={}, reason={}", transactionId, reason);
        
        EscrowTransaction transaction = escrowTransactionRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new EscrowNotFoundException(transactionId));
        
        EscrowStatus previousStatus = transaction.getStatus();
        transaction.cancel(reason);
        
        // 입금이 완료된 경우 환불 금액 계산
        BigDecimal refundAmount = BigDecimal.ZERO;
        if (previousStatus == EscrowStatus.DEPOSITED || 
            previousStatus == EscrowStatus.DELIVERED ||
            previousStatus == EscrowStatus.VERIFIED ||
            previousStatus == EscrowStatus.OWNERSHIP_TRANSFERRED) {
            refundAmount = transaction.getAmount();
        }
        
        // 이벤트 소싱에 기록
        eventSourcingService.storeEscrowEvent(
            transaction.getTransactionId(),
            "EscrowCancelled",
            previousStatus.name(),
            "CANCELLED",
            Map.of(
                "reason", reason,
                "refundAmount", refundAmount,
                "refundTo", transaction.getBuyer().getUserId()
            ),
            "SYSTEM"
        );
        
        // 이벤트 발행
        EscrowCancelledEvent event = new EscrowCancelledEvent(
            transaction.getTransactionId(),
            reason,
            refundAmount,
            transaction.getBuyer().getUserId()
        );
        eventPublisher.publish(event);
        
        log.info("에스크로 거래 취소 완료: transactionId={}", transactionId);
        
        return EscrowResponse.from(transaction);
    }
    
    @Transactional(readOnly = true)
    public List<EscrowResponse> getAllEscrows() {
        List<EscrowTransaction> transactions = escrowTransactionRepository.findAll();
        return transactions.stream()
            .map(EscrowResponse::from)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<EscrowResponse> getEscrowsByStatus(EscrowStatus status) {
        List<EscrowTransaction> transactions = escrowTransactionRepository.findByStatus(status);
        return transactions.stream()
            .map(EscrowResponse::from)
            .collect(Collectors.toList());
    }
}
