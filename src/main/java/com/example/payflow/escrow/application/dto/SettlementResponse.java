package com.example.payflow.escrow.application.dto;

import com.example.payflow.escrow.domain.Settlement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SettlementResponse {
    private Long id;
    private String transactionId;
    private BigDecimal totalAmount;
    private BigDecimal feeAmount;
    private BigDecimal sellerAmount;
    private String sellerId;
    private String paymentMethod;
    private String paymentReference;
    private LocalDateTime initiatedAt;
    private LocalDateTime completedAt;
    private String failureReason;
    private boolean completed;
    private boolean failed;
    
    public static SettlementResponse from(Settlement settlement) {
        return new SettlementResponse(
            settlement.getId(),
            settlement.getTransactionId(),
            settlement.getTotalAmount(),
            settlement.getFeeAmount(),
            settlement.getSellerAmount(),
            settlement.getSellerId(),
            settlement.getPaymentMethod(),
            settlement.getPaymentReference(),
            settlement.getInitiatedAt(),
            settlement.getCompletedAt(),
            settlement.getFailureReason(),
            settlement.isCompleted(),
            settlement.isFailed()
        );
    }
}
