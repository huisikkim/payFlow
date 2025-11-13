package com.example.payflow.escrow.application;

import com.example.payflow.escrow.application.dto.DepositRequest;
import com.example.payflow.escrow.application.dto.DepositResponse;
import com.example.payflow.escrow.application.dto.VirtualAccountDepositResponse;
import com.example.payflow.escrow.domain.*;
import com.example.payflow.payment.infrastructure.TossPaymentsClient;
import com.example.payflow.payment.infrastructure.dto.TossPaymentConfirmRequest;
import com.example.payflow.payment.infrastructure.dto.TossPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 가상계좌 입금 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VirtualAccountService {
    
    private final VirtualAccountDepositRepository virtualAccountDepositRepository;
    private final EscrowTransactionRepository escrowTransactionRepository;
    private final TossPaymentsClient tossPaymentsClient;
    private final DepositService depositService;
    
    /**
     * 가상계좌 발급 및 입금 대기
     */
    @Transactional
    public VirtualAccountDepositResponse issueVirtualAccount(
            String transactionId,
            String paymentKey,
            String orderId,
            BigDecimal amount) {
        
        log.info("가상계좌 발급 시작: transactionId={}, paymentKey={}", transactionId, paymentKey);
        
        // 거래 조회
        EscrowTransaction transaction = escrowTransactionRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new EscrowNotFoundException(transactionId));
        
        // 금액 검증
        if (transaction.getAmount().compareTo(amount) != 0) {
            throw new IllegalArgumentException("결제 금액이 에스크로 거래 금액과 일치하지 않습니다");
        }
        
        try {
            // 토스 페이먼츠 결제 승인 (가상계좌 발급)
            TossPaymentConfirmRequest confirmRequest = new TossPaymentConfirmRequest(
                paymentKey,
                orderId,
                amount.longValue()
            );
            
            TossPaymentResponse tossResponse = tossPaymentsClient.confirmPayment(confirmRequest);
            
            log.info("토스 가상계좌 발급 완료: paymentKey={}, accountNumber={}", 
                tossResponse.getPaymentKey(), 
                tossResponse.getVirtualAccount() != null ? tossResponse.getVirtualAccount().getAccountNumber() : "N/A");
            
            // 가상계좌 정보 검증
            if (tossResponse.getVirtualAccount() == null) {
                throw new IllegalStateException("가상계좌 정보가 없습니다");
            }
            
            TossPaymentResponse.VirtualAccountInfo vaInfo = tossResponse.getVirtualAccount();
            log.info("가상계좌 상세 정보: accountNumber={}, bankCode={}, bank={}, dueDate={}", 
                vaInfo.getAccountNumber(), vaInfo.getBankCode(), vaInfo.getBank(), vaInfo.getDueDate());
            
            // 은행명이 없으면 은행코드로 매핑
            String bankName = vaInfo.getBank();
            if (bankName == null || bankName.isEmpty()) {
                bankName = getBankNameFromCode(vaInfo.getBankCode());
                log.warn("은행명이 없어서 은행코드로 매핑: bankCode={}, bankName={}", vaInfo.getBankCode(), bankName);
            }
            
            // 토스페이먼츠 날짜 형식: "2025-11-15T07:52:36+09:00" (ISO 8601 with timezone)
            LocalDateTime dueDate = java.time.OffsetDateTime.parse(vaInfo.getDueDate())
                .toLocalDateTime();
            
            VirtualAccountDeposit virtualAccountDeposit = new VirtualAccountDeposit(
                transactionId,
                amount,
                vaInfo.getAccountNumber(),
                vaInfo.getBankCode(),
                bankName,
                dueDate,
                tossResponse.getPaymentKey(),
                orderId
            );
            
            virtualAccountDepositRepository.save(virtualAccountDeposit);
            
            log.info("가상계좌 정보 저장 완료: id={}, accountNumber={}", 
                virtualAccountDeposit.getId(), virtualAccountDeposit.getVirtualAccountNumber());
            
            return VirtualAccountDepositResponse.from(virtualAccountDeposit);
            
        } catch (Exception e) {
            log.error("가상계좌 발급 실패: transactionId={}", transactionId, e);
            throw new RuntimeException("가상계좌 발급 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 가상계좌 입금 완료 처리 (웹훅)
     */
    @Transactional
    public DepositResponse completeVirtualAccountDeposit(String orderId, String depositorName) {
        log.info("가상계좌 입금 완료 처리: orderId={}, depositorName={}", orderId, depositorName);
        
        VirtualAccountDeposit virtualAccountDeposit = virtualAccountDepositRepository.findByOrderId(orderId)
            .orElseThrow(() -> new IllegalArgumentException("가상계좌를 찾을 수 없습니다: " + orderId));
        
        // 입금 완료 처리
        virtualAccountDeposit.completeDeposit(depositorName);
        
        // 에스크로 입금 처리
        DepositRequest depositRequest = DepositRequest.builder()
            .transactionId(virtualAccountDeposit.getTransactionId())
            .amount(virtualAccountDeposit.getAmount())
            .depositMethod("VIRTUAL_ACCOUNT")
            .depositReference(virtualAccountDeposit.getVirtualAccountNumber())
            .build();
        
        DepositResponse depositResponse = depositService.processDeposit(depositRequest);
        
        log.info("가상계좌 입금 완료: transactionId={}, depositId={}", 
            virtualAccountDeposit.getTransactionId(), depositResponse.getId());
        
        return depositResponse;
    }
    
    /**
     * 가상계좌 취소 처리 (웹훅)
     */
    @Transactional
    public void cancelVirtualAccount(String orderId, String reason) {
        log.info("가상계좌 취소 처리: orderId={}, reason={}", orderId, reason);
        
        VirtualAccountDeposit virtualAccountDeposit = virtualAccountDepositRepository.findByOrderId(orderId)
            .orElseThrow(() -> new IllegalArgumentException("가상계좌를 찾을 수 없습니다: " + orderId));
        
        virtualAccountDeposit.cancel(reason);
        
        log.info("가상계좌 취소 완료: id={}", virtualAccountDeposit.getId());
    }
    
    /**
     * 거래별 가상계좌 목록 조회
     */
    @Transactional(readOnly = true)
    public List<VirtualAccountDepositResponse> getVirtualAccountsByTransaction(String transactionId) {
        return virtualAccountDepositRepository.findByTransactionId(transactionId).stream()
            .map(VirtualAccountDepositResponse::from)
            .collect(Collectors.toList());
    }
    
    /**
     * 가상계좌 상세 조회
     */
    @Transactional(readOnly = true)
    public VirtualAccountDepositResponse getVirtualAccount(String orderId) {
        VirtualAccountDeposit virtualAccountDeposit = virtualAccountDepositRepository.findByOrderId(orderId)
            .orElseThrow(() -> new IllegalArgumentException("가상계좌를 찾을 수 없습니다: " + orderId));
        
        return VirtualAccountDepositResponse.from(virtualAccountDeposit);
    }
    
    /**
     * 은행 코드로 은행명 매핑
     */
    private String getBankNameFromCode(String bankCode) {
        if (bankCode == null) {
            return "알 수 없는 은행";
        }
        
        // 토스페이먼츠 은행 코드 매핑
        return switch (bankCode) {
            case "39" -> "경남은행";
            case "34" -> "광주은행";
            case "04" -> "국민은행";
            case "03" -> "기업은행";
            case "11" -> "농협은행";
            case "31" -> "대구은행";
            case "32" -> "부산은행";
            case "02" -> "산업은행";
            case "45" -> "새마을금고";
            case "07" -> "수협은행";
            case "88" -> "신한은행";
            case "48" -> "신협";
            case "05" -> "외환은행";
            case "20" -> "우리은행";
            case "71" -> "우체국";
            case "50" -> "저축은행";
            case "37" -> "전북은행";
            case "35" -> "제주은행";
            case "90" -> "카카오뱅크";
            case "89" -> "케이뱅크";
            case "92" -> "토스뱅크";
            case "81" -> "하나은행";
            default -> bankCode + "은행";
        };
    }
}
