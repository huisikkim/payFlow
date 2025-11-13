package com.example.payflow.escrow.application;

import com.example.payflow.escrow.application.dto.DepositRequest;
import com.example.payflow.escrow.application.dto.DepositResponse;
import com.example.payflow.escrow.domain.EscrowNotFoundException;
import com.example.payflow.escrow.domain.EscrowTransaction;
import com.example.payflow.escrow.domain.EscrowTransactionRepository;
import com.example.payflow.payment.infrastructure.TossPaymentsClient;
import com.example.payflow.payment.infrastructure.dto.TossPaymentConfirmRequest;
import com.example.payflow.payment.infrastructure.dto.TossPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 에스크로 결제 통합 서비스
 * 토스 페이먼츠를 통한 에스크로 입금 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EscrowPaymentService {
    
    private final EscrowTransactionRepository escrowTransactionRepository;
    private final TossPaymentsClient tossPaymentsClient;
    private final DepositService depositService;
    
    /**
     * 에스크로 입금을 위한 결제 정보 생성
     */
    @Transactional(readOnly = true)
    public EscrowPaymentInfo createPaymentInfo(String transactionId) {
        EscrowTransaction transaction = escrowTransactionRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new EscrowNotFoundException(transactionId));
        
        // 에스크로 거래 금액을 결제 금액으로 사용
        return EscrowPaymentInfo.builder()
            .transactionId(transaction.getTransactionId())
            .orderId("ESCROW-" + transaction.getTransactionId())
            .orderName("에스크로 입금 - " + transaction.getVehicle().getManufacturer() + " " + transaction.getVehicle().getModel())
            .amount(transaction.getAmount())
            .customerEmail(transaction.getBuyer().getEmail())
            .customerName(transaction.getBuyer().getName())
            .build();
    }
    
    /**
     * 토스 결제 승인 후 에스크로 입금 처리
     */
    @Transactional
    public DepositResponse confirmPaymentAndDeposit(
            String transactionId,
            String paymentKey,
            String orderId,
            BigDecimal amount) {
        
        log.info("에스크로 결제 승인 및 입금 처리 시작: transactionId={}, paymentKey={}", 
            transactionId, paymentKey);
        
        // 거래 조회
        EscrowTransaction transaction = escrowTransactionRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new EscrowNotFoundException(transactionId));
        
        // 금액 검증
        if (transaction.getAmount().compareTo(amount) != 0) {
            throw new IllegalArgumentException("결제 금액이 에스크로 거래 금액과 일치하지 않습니다");
        }
        
        try {
            // 토스 페이먼츠 결제 승인
            TossPaymentConfirmRequest confirmRequest = new TossPaymentConfirmRequest(
                paymentKey,
                orderId,
                amount.longValue()
            );
            
            TossPaymentResponse tossResponse = tossPaymentsClient.confirmPayment(confirmRequest);
            
            log.info("토스 결제 승인 완료: paymentKey={}, method={}", 
                tossResponse.getPaymentKey(), tossResponse.getMethod());
            
            // 에스크로 입금 처리
            DepositRequest depositRequest = DepositRequest.builder()
                .transactionId(transactionId)
                .amount(amount)
                .depositMethod("TOSS_" + tossResponse.getMethod())
                .depositReference(tossResponse.getPaymentKey())
                .build();
            
            DepositResponse depositResponse = depositService.processDeposit(depositRequest);
            
            log.info("에스크로 입금 처리 완료: transactionId={}, depositId={}", 
                transactionId, depositResponse.getId());
            
            return depositResponse;
            
        } catch (Exception e) {
            log.error("에스크로 결제 승인 또는 입금 처리 실패: transactionId={}", transactionId, e);
            throw new RuntimeException("에스크로 결제 처리 실패: " + e.getMessage(), e);
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class EscrowPaymentInfo {
        private String transactionId;
        private String orderId;
        private String orderName;
        private BigDecimal amount;
        private String customerEmail;
        private String customerName;
    }
}
