package com.example.payflow.escrow.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class VehicleVerifiedEvent implements DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final String transactionId;
    private final String verifiedBy;
    private final String vehicleVin;
    
    public VehicleVerifiedEvent(String transactionId, String verifiedBy, String vehicleVin) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.transactionId = transactionId;
        this.verifiedBy = verifiedBy;
        this.vehicleVin = vehicleVin;
    }
    
    @Override
    public String getEventType() {
        return "VehicleVerified";
    }
}
