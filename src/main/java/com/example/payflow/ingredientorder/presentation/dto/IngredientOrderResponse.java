package com.example.payflow.ingredientorder.presentation.dto;

import com.example.payflow.ingredientorder.domain.IngredientOrder;
import com.example.payflow.ingredientorder.domain.IngredientOrderStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class IngredientOrderResponse {
    
    private final String orderId;
    private final String storeId;
    private final String distributorId;
    private final IngredientOrderStatus status;
    private final Long totalAmount;
    private final List<IngredientOrderItemDto> items;
    private final String rejectionReason;
    private final LocalDateTime orderedAt;
    private final LocalDateTime confirmedAt;
    private final LocalDateTime completedAt;
    
    public IngredientOrderResponse(String orderId, String storeId, String distributorId, 
                                  IngredientOrderStatus status, Long totalAmount,
                                  List<IngredientOrderItemDto> items, String rejectionReason,
                                  LocalDateTime orderedAt, LocalDateTime confirmedAt, 
                                  LocalDateTime completedAt) {
        this.orderId = orderId;
        this.storeId = storeId;
        this.distributorId = distributorId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.items = items;
        this.rejectionReason = rejectionReason;
        this.orderedAt = orderedAt;
        this.confirmedAt = confirmedAt;
        this.completedAt = completedAt;
    }
    
    public static IngredientOrderResponse from(IngredientOrder order) {
        List<IngredientOrderItemDto> items = order.getItems().stream()
            .map(IngredientOrderItemDto::from)
            .collect(Collectors.toList());
        
        return new IngredientOrderResponse(
            order.getOrderId(),
            order.getStoreId(),
            order.getDistributorId(),
            order.getStatus(),
            order.getTotalAmount(),
            items,
            order.getRejectionReason(),
            order.getOrderedAt(),
            order.getConfirmedAt(),
            order.getCompletedAt()
        );
    }
}
