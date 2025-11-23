package com.example.payflow.ingredientorder.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class IngredientOrderCreatedEvent implements DomainEvent {
    
    private final String eventId;
    private final String orderId;
    private final String storeId;
    private final String distributorId;
    private final Long totalAmount;
    private final LocalDateTime occurredOn;
    
    public IngredientOrderCreatedEvent(String orderId, String storeId, String distributorId, Long totalAmount) {
        this.eventId = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.storeId = storeId;
        this.distributorId = distributorId;
        this.totalAmount = totalAmount;
        this.occurredOn = LocalDateTime.now();
    }
    
    @Override
    public String getEventType() {
        return "IngredientOrderCreated";
    }
}
