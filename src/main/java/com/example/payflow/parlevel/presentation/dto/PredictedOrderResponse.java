package com.example.payflow.parlevel.presentation.dto;

import com.example.payflow.parlevel.domain.PredictedOrder;
import com.example.payflow.parlevel.domain.PredictionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PredictedOrderResponse {
    private Long id;
    private String storeId;
    private String itemName;
    private Integer currentStock;
    private Integer predictedConsumption;
    private Integer recommendedOrderQuantity;
    private LocalDate predictedOrderDate;
    private LocalDate expectedDeliveryDate;
    private PredictionStatus status;
    private String actualOrderId;
    private String reason;
    private LocalDateTime createdAt;
    
    public static PredictedOrderResponse from(PredictedOrder prediction) {
        return new PredictedOrderResponse(
            prediction.getId(),
            prediction.getStoreId(),
            prediction.getItemName(),
            prediction.getCurrentStock(),
            prediction.getPredictedConsumption(),
            prediction.getRecommendedOrderQuantity(),
            prediction.getPredictedOrderDate(),
            prediction.getExpectedDeliveryDate(),
            prediction.getStatus(),
            prediction.getActualOrderId(),
            prediction.getReason(),
            prediction.getCreatedAt()
        );
    }
}
