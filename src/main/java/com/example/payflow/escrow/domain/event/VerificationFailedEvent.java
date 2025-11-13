package com.example.payflow.escrow.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class VerificationFailedEvent implements DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final String transactionId;
    private final String verifiedBy;
    private final String reason;
    
    public VerificationFailedEvent(String transactionId, String verifiedBy, String reason) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.transactionId = transactionId;
        this.verifiedBy = verifiedBy;
        this.reason = reason;
    }
    
    @Override
    public String getEventType() {
        return "VerificationFailed";
    }
}
