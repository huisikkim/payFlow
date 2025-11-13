package com.example.payflow.escrow.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class EscrowCompletedEvent implements DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final String transactionId;
    private final String sellerId;
    private final BigDecimal sellerAmount;
    private final BigDecimal feeAmount;
    
    public EscrowCompletedEvent(String transactionId, String sellerId, 
                               BigDecimal sellerAmount, BigDecimal feeAmount) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.transactionId = transactionId;
        this.sellerId = sellerId;
        this.sellerAmount = sellerAmount;
        this.feeAmount = feeAmount;
    }
    
    @Override
    public String getEventType() {
        return "EscrowCompleted";
    }
}
