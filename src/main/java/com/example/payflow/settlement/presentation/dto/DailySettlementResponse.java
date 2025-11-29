package com.example.payflow.settlement.presentation.dto;

import com.example.payflow.settlement.domain.DailySettlement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySettlementResponse {
    
    private Long id;
    private LocalDate settlementDate;
    private String storeId;
    private String distributorId;
    private Integer orderCount;
    private Long totalSalesAmount;
    private Long totalSettlementAmount;
    private Long totalPaidAmount;
    private Long totalOutstandingAmount;
    private Integer catalogOrderCount;
    private Long catalogSalesAmount;
    private Integer ingredientOrderCount;
    private Long ingredientSalesAmount;
    private Double paymentRate; // 결제율 (%)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static DailySettlementResponse from(DailySettlement settlement) {
        double paymentRate = settlement.getTotalSalesAmount() > 0
            ? (double) settlement.getTotalPaidAmount() / settlement.getTotalSalesAmount() * 100
            : 0.0;
        
        return DailySettlementResponse.builder()
            .id(settlement.getId())
            .settlementDate(settlement.getSettlementDate())
            .storeId(settlement.getStoreId())
            .distributorId(settlement.getDistributorId())
            .orderCount(settlement.getOrderCount())
            .totalSalesAmount(settlement.getTotalSalesAmount())
            .totalSettlementAmount(settlement.getTotalSettlementAmount())
            .totalPaidAmount(settlement.getTotalPaidAmount())
            .totalOutstandingAmount(settlement.getTotalOutstandingAmount())
            .catalogOrderCount(settlement.getCatalogOrderCount())
            .catalogSalesAmount(settlement.getCatalogSalesAmount())
            .ingredientOrderCount(settlement.getIngredientOrderCount())
            .ingredientSalesAmount(settlement.getIngredientSalesAmount())
            .paymentRate(paymentRate)
            .createdAt(settlement.getCreatedAt())
            .updatedAt(settlement.getUpdatedAt())
            .build();
    }
}
