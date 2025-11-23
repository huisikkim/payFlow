package com.example.payflow.ingredientorder.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IngredientOrderRejectedEvent implements DomainEvent {
    
    private String eventId;
    private String orderId;
    private String storeId;
    private String distributorId;
    private String rejectionReason;
    private LocalDateTime occurredOn;
    
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
