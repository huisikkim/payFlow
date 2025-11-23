package com.example.payflow.settlement.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class SettlementCompletedEvent implements DomainEvent {
    
    private final String eventId;
    private final String settlementId;
    private final String orderId;
    private final String storeId;
    private final Long paidAmount;
    private final Long outstandingAmount;
    private final LocalDateTime occurredOn;
    
    public SettlementCompletedEvent(String settlementId, String orderId, String storeId, 
                                   Long paidAmount, Long outstandingAmount) {
        this.eventId = UUID.randomUUID().toString();
        this.settlementId = settlementId;
        this.orderId = orderId;
        this.storeId = storeId;
        this.paidAmount = paidAmount;
        this.outstandingAmount = outstandingAmount;
        this.occurredOn = LocalDateTime.now();
    }
    
    @Override
    public String getEventType() {
        return "SettlementCompleted";
    }
}
