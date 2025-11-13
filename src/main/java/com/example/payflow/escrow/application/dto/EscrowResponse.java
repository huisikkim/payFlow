package com.example.payflow.escrow.application.dto;

import com.example.payflow.escrow.domain.EscrowStatus;
import com.example.payflow.escrow.domain.EscrowTransaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EscrowResponse {
    private Long id;
    private String transactionId;
    private ParticipantDto buyer;
    private ParticipantDto seller;
    private VehicleDto vehicle;
    private BigDecimal amount;
    private BigDecimal feeRate;
    private BigDecimal feeAmount;
    private BigDecimal sellerAmount;
    private EscrowStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    
    public static EscrowResponse from(EscrowTransaction transaction) {
        return new EscrowResponse(
            transaction.getId(),
            transaction.getTransactionId(),
            ParticipantDto.from(transaction.getBuyer()),
            ParticipantDto.from(transaction.getSeller()),
            VehicleDto.from(transaction.getVehicle()),
            transaction.getAmount(),
            transaction.getFeeRate(),
            transaction.calculateFee(),
            transaction.calculateSellerAmount(),
            transaction.getStatus(),
            transaction.getCreatedAt(),
            transaction.getUpdatedAt(),
            transaction.getCompletedAt()
        );
    }
}
