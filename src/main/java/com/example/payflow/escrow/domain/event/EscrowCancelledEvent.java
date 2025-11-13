package com.example.payflow.escrow.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class EscrowCancelledEvent implements DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final String transactionId;
    private final String reason;
    private final BigDecimal refundAmount;
    private final String refundTo;
    
    public EscrowCancelledEvent(String transactionId, String reason, 
                               BigDecimal refundAmount, String refundTo) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.transactionId = transactionId;
        this.reason = reason;
        this.refundAmount = refundAmount;
        this.refundTo = refundTo;
    }
    
    @Override
    public String getEventType() {
        return "EscrowCancelled";
    }
}
