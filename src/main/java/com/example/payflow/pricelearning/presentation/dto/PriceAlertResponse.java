package com.example.payflow.pricelearning.presentation.dto;

import com.example.payflow.pricelearning.domain.PriceAlert;
import com.example.payflow.pricelearning.domain.PriceAlertStatus;
import com.example.payflow.pricelearning.domain.PriceAlertType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PriceAlertResponse {
    private String alertId;
    private String itemName;
    private Long currentPrice;
    private Long averagePrice;
    private Double surgePercentage;
    private PriceAlertType alertType;
    private PriceAlertStatus status;
    private String orderId;
    private String distributorId;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime resolvedAt;
    
    public static PriceAlertResponse from(PriceAlert alert) {
        return new PriceAlertResponse(
            alert.getAlertId(),
            alert.getItemName(),
            alert.getCurrentPrice(),
            alert.getAveragePrice(),
            alert.getSurgePercentage(),
            alert.getAlertType(),
            alert.getStatus(),
            alert.getOrderId(),
            alert.getDistributorId(),
            alert.getMessage(),
            alert.getCreatedAt(),
            alert.getAcknowledgedAt(),
            alert.getResolvedAt()
        );
    }
}
