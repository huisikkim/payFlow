package com.example.payflow.catalog.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CatalogOrderPaymentCompletedEvent implements DomainEvent {
    
    private String eventId;
    private String orderNumber;
    private Long orderId;
    private String storeId;
    private String distributorId;
    private Long totalAmount;
    private LocalDateTime occurredOn;
    
    public CatalogOrderPaymentCompletedEvent(String orderNumber, Long orderId, String storeId, 
                                            String distributorId, Long totalAmount) {
        this.eventId = UUID.randomUUID().toString();
        this.orderNumber = orderNumber;
        this.orderId = orderId;
        this.storeId = storeId;
        this.distributorId = distributorId;
        this.totalAmount = totalAmount;
        this.occurredOn = LocalDateTime.now();
    }
    
    @Override
    public String getEventType() {
        return "CatalogOrderPaymentCompleted";
    }
}
