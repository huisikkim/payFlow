package com.example.payflow.escrow.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class DisputeResolvedEvent implements DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final String transactionId;
    private final String resolvedBy;
    private final String resolution;
    
    public DisputeResolvedEvent(String transactionId, String resolvedBy, String resolution) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.transactionId = transactionId;
        this.resolvedBy = resolvedBy;
        this.resolution = resolution;
    }
    
    @Override
    public String getEventType() {
        return "DisputeResolved";
    }
}
