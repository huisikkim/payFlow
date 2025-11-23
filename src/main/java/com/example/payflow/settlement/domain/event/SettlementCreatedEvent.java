package com.example.payflow.settlement.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class SettlementCreatedEvent implements DomainEvent {
    
    private final String eventId;
    private final String settlementId;
    private final String orderId;
    private final String storeId;
    private final String distributorId;
    private final Long settlementAmount;
    private final LocalDateTime occurredOn;
    
    public SettlementCreatedEvent(String settlementId, String orderId, String storeId, 
                                 String distributorId, Long settlementAmount) {
        this.eventId = UUID.randomUUID().toString();
        this.settlementId = settlementId;
        this.orderId = orderId;
        this.storeId = storeId;
        this.distributorId = distributorId;
        this.settlementAmount = settlementAmount;
        this.occurredOn = LocalDateTime.now();
    }
    
    @Override
    public String getEventType() {
        return "SettlementCreated";
    }
}
