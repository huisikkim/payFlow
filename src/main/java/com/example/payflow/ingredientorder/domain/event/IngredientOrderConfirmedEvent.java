package com.example.payflow.ingredientorder.domain.event;

import com.example.payflow.common.event.DomainEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IngredientOrderConfirmedEvent implements DomainEvent {
    
    private String eventId;
    private String orderId;
    private String storeId;
    private String distributorId;
    private Long totalAmount;
    private LocalDateTime occurredOn;
    
    public IngredientOrderConfirmedEvent(String orderId, String storeId, String distributorId, Long totalAmount) {
        this.eventId = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.storeId = storeId;
        this.distributorId = distributorId;
        this.totalAmount = totalAmount;
        this.occurredOn = LocalDateTime.now();
    }
    
    @Override
    public String getEventType() {
        return "IngredientOrderConfirmed";
    }
}
