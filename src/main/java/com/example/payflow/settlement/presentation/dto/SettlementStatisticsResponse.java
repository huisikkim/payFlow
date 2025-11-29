package com.example.payflow.settlement.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementStatisticsResponse {
    
    private String type; // "STORE" or "DISTRIBUTOR"
    private String id; // storeId or distributorId
    private Integer totalOrderCount;
    private Long totalSalesAmount;
    private Long totalPaidAmount;
    private Long totalOutstandingAmount;
    private Integer catalogOrderCount;
    private Long catalogSalesAmount;
    private Integer ingredientOrderCount;
    private Long ingredientSalesAmount;
    private Double paymentRate; // 결제율 (%)
}
