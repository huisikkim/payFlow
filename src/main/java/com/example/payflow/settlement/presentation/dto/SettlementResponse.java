package com.example.payflow.settlement.presentation.dto;

import com.example.payflow.settlement.domain.IngredientSettlement;
import com.example.payflow.settlement.domain.SettlementStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SettlementResponse {
    
    private final String settlementId;
    private final String storeId;
    private final String distributorId;
    private final String orderId;
    private final Long settlementAmount;
    private final Long outstandingAmount;
    private final Long paidAmount;
    private final SettlementStatus status;
    private final LocalDateTime settlementDate;
    private final LocalDateTime completedAt;
    
    public SettlementResponse(String settlementId, String storeId, String distributorId, 
                             String orderId, Long settlementAmount, Long outstandingAmount,
                             Long paidAmount, SettlementStatus status, 
                             LocalDateTime settlementDate, LocalDateTime completedAt) {
        this.settlementId = settlementId;
        this.storeId = storeId;
        this.distributorId = distributorId;
        this.orderId = orderId;
        this.settlementAmount = settlementAmount;
        this.outstandingAmount = outstandingAmount;
        this.paidAmount = paidAmount;
        this.status = status;
        this.settlementDate = settlementDate;
        this.completedAt = completedAt;
    }
    
    public static SettlementResponse from(IngredientSettlement settlement) {
        return new SettlementResponse(
            settlement.getSettlementId(),
            settlement.getStoreId(),
            settlement.getDistributorId(),
            settlement.getOrderId(),
            settlement.getSettlementAmount(),
            settlement.getOutstandingAmount(),
            settlement.getPaidAmount(),
            settlement.getStatus(),
            settlement.getSettlementDate(),
            settlement.getCompletedAt()
        );
    }
}
