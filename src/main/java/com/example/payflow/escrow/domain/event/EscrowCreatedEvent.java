package com.example.payflow.escrow.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class EscrowCreatedEvent implements DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final String transactionId;
    private final String buyerId;
    private final String sellerId;
    private final BigDecimal amount;
    private final String vehicleVin;
    
    public EscrowCreatedEvent(String transactionId, String buyerId, String sellerId, 
                             BigDecimal amount, String vehicleVin) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.transactionId = transactionId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.amount = amount;
        this.vehicleVin = vehicleVin;
    }
    
    @Override
    public String getEventType() {
        return "EscrowCreated";
    }
}
