package com.example.payflow.escrow.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class SettlementFailedEvent implements DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final String transactionId;
    private final String reason;
    
    public SettlementFailedEvent(String transactionId, String reason) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.transactionId = transactionId;
        this.reason = reason;
    }
    
    @Override
    public String getEventType() {
        return "SettlementFailed";
    }
}
