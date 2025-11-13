package com.example.payflow.escrow.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class DepositConfirmedEvent implements DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final String transactionId;
    private final BigDecimal amount;
    private final String depositMethod;
    
    public DepositConfirmedEvent(String transactionId, BigDecimal amount, String depositMethod) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.transactionId = transactionId;
        this.amount = amount;
        this.depositMethod = depositMethod;
    }
    
    @Override
    public String getEventType() {
        return "DepositConfirmed";
    }
}
