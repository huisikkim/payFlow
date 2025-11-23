package com.example.payflow.ingredientorder.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class IngredientOrderRejectedEvent implements DomainEvent {
    
    private final String eventId;
    private final String orderId;
    private final String storeId;
    private final String distributorId;
    private final String rejectionReason;
    private final LocalDateTime occurredOn;
    
    public IngredientOrderRejectedEvent(String orderId, String storeId, String distributorId, String rejectionReason) {
        this.eventId = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.storeId = storeId;
        this.distributorId = distributorId;
        this.rejectionReason = rejectionReason;
        this.occurredOn = LocalDateTime.now();
    }
    
    @Override
    public String getEventType() {
        return "IngredientOrderRejected";
    }
}
