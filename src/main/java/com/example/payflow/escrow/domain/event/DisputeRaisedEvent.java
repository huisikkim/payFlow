package com.example.payflow.escrow.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class DisputeRaisedEvent implements DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final String transactionId;
    private final String raisedBy;
    private final String reason;
    
    public DisputeRaisedEvent(String transactionId, String raisedBy, String reason) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.transactionId = transactionId;
        this.raisedBy = raisedBy;
        this.reason = reason;
    }
    
    @Override
    public String getEventType() {
        return "DisputeRaised";
    }
}
