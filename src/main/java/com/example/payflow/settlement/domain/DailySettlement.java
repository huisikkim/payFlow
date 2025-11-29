package com.example.payflow.settlement.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_settlements", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"settlement_date", "store_id", "distributor_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailySettlement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDate settlementDate; // 정산 기준일
    
    @Column(nullable = false)
    private String storeId;
    
    @Column(nullable = false)
    private String distributorId;
    
    @Column(nullable = false)
    private Integer orderCount; // 주문 건수
    
    @Column(nullable = false)
    private Long totalSalesAmount; // 총 매출액
    
    @Column(nullable = false)
    private Long totalSettlementAmount; // 총 정산 금액
    
    @Column(nullable = false)
    private Long totalPaidAmount; // 총 지불 완료 금액
    
    @Column(nullable = false)
    private Long totalOutstandingAmount; // 총 미수금
    
    @Column(nullable = false)
    private Integer catalogOrderCount; // 카탈로그 주문 건수
    
    @Column(nullable = false)
    private Long catalogSalesAmount; // 카탈로그 매출액
    
    @Column(nullable = false)
    private Integer ingredientOrderCount; // 식자재 주문 건수
    
    @Column(nullable = false)
    private Long ingredientSalesAmount; // 식자재 매출액
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public DailySettlement(LocalDate settlementDate, String storeId, String distributorId) {
        this.settlementDate = settlementDate;
        this.storeId = storeId;
        this.distributorId = distributorId;
        this.orderCount = 0;
        this.totalSalesAmount = 0L;
        this.totalSettlementAmount = 0L;
        this.totalPaidAmount = 0L;
        this.totalOutstandingAmount = 0L;
        this.catalogOrderCount = 0;
        this.catalogSalesAmount = 0L;
        this.ingredientOrderCount = 0;
        this.ingredientSalesAmount = 0L;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void addOrder(String orderType, Long amount, Long paidAmount, Long outstandingAmount) {
        this.orderCount++;
        this.totalSalesAmount += amount;
        this.totalSettlementAmount += amount;
        this.totalPaidAmount += paidAmount;
        this.totalOutstandingAmount += outstandingAmount;
        
        if ("CATALOG".equals(orderType)) {
            this.catalogOrderCount++;
            this.catalogSalesAmount += amount;
        } else if ("INGREDIENT".equals(orderType)) {
            this.ingredientOrderCount++;
            this.ingredientSalesAmount += amount;
        }
        
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updatePayment(Long additionalPaidAmount) {
        this.totalPaidAmount += additionalPaidAmount;
        this.totalOutstandingAmount -= additionalPaidAmount;
        this.updatedAt = LocalDateTime.now();
    }
}
