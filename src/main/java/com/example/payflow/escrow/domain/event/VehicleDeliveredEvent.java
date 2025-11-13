package com.example.payflow.escrow.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class VehicleDeliveredEvent implements DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final String transactionId;
    private final String vehicleVin;
    
    public VehicleDeliveredEvent(String transactionId, String vehicleVin) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.transactionId = transactionId;
        this.vehicleVin = vehicleVin;
    }
    
    @Override
    public String getEventType() {
        return "VehicleDelivered";
    }
}
