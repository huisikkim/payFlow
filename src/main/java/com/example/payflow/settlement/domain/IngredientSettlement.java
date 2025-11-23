package com.example.payflow.settlement.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ingredient_settlements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IngredientSettlement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String settlementId;
    
    @Column(nullable = false)
    private String storeId;
    
    @Column(nullable = false)
    private String distributorId;
    
    @Column(nullable = false)
    private String orderId;
    
    @Column(nullable = false)
    private Long settlementAmount;
    
    @Column(nullable = false)
    private Long outstandingAmount; // 미수금
    
    @Column(nullable = false)
    private Long paidAmount; // 지불 완료 금액
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;
    
    private LocalDateTime settlementDate;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public IngredientSettlement(String settlementId, String storeId, String distributorId, 
                               String orderId, Long settlementAmount) {
        this.settlementId = settlementId;
        this.storeId = storeId;
        this.distributorId = distributorId;
        this.orderId = orderId;
        this.settlementAmount = settlementAmount;
        this.outstandingAmount = settlementAmount; // 초기에는 전액 미수금
        this.paidAmount = 0L;
        this.status = SettlementStatus.PENDING;
        this.settlementDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void startProcessing() {
        this.status = SettlementStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void complete(Long paidAmount) {
        this.paidAmount = paidAmount;
        this.outstandingAmount = this.settlementAmount - paidAmount;
        this.status = SettlementStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void fail() {
        this.status = SettlementStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateOutstanding(Long paidAmount) {
        this.paidAmount += paidAmount;
        this.outstandingAmount = this.settlementAmount - this.paidAmount;
        this.updatedAt = LocalDateTime.now();
    }
}
