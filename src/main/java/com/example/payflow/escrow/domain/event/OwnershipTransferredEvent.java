package com.example.payflow.escrow.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class OwnershipTransferredEvent implements DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final String transactionId;
    private final String vehicleVin;
    private final String newOwnerId;
    
    public OwnershipTransferredEvent(String transactionId, String vehicleVin, String newOwnerId) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.transactionId = transactionId;
        this.vehicleVin = vehicleVin;
        this.newOwnerId = newOwnerId;
    }
    
    @Override
    public String getEventType() {
        return "OwnershipTransferred";
    }
}
