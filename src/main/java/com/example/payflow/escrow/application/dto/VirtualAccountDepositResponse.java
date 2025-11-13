package com.example.payflow.escrow.application.dto;

import com.example.payflow.escrow.domain.VirtualAccountDeposit;
import com.example.payflow.escrow.domain.VirtualAccountStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class VirtualAccountDepositResponse {
    
    private Long id;
    private String transactionId;
    private BigDecimal amount;
    private String virtualAccountNumber;
    private String bankCode;
    private String bankName;
    private LocalDateTime dueDate;
    private String depositorName;
    private VirtualAccountStatus status;
    private String paymentKey;
    private String orderId;
    private LocalDateTime createdAt;
    private LocalDateTime depositedAt;
    private LocalDateTime canceledAt;
    private String cancelReason;
    
    public static VirtualAccountDepositResponse from(VirtualAccountDeposit deposit) {
        return VirtualAccountDepositResponse.builder()
            .id(deposit.getId())
            .transactionId(deposit.getTransactionId())
            .amount(deposit.getAmount())
            .virtualAccountNumber(deposit.getVirtualAccountNumber())
            .bankCode(deposit.getBankCode())
            .bankName(deposit.getBankName())
            .dueDate(deposit.getDueDate())
            .depositorName(deposit.getDepositorName())
            .status(deposit.getStatus())
            .paymentKey(deposit.getPaymentKey())
            .orderId(deposit.getOrderId())
            .createdAt(deposit.getCreatedAt())
            .depositedAt(deposit.getDepositedAt())
            .canceledAt(deposit.getCanceledAt())
            .cancelReason(deposit.getCancelReason())
            .build();
    }
}
