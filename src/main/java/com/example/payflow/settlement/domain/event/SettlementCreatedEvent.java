package com.example.payflow.settlement.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SettlementCreatedEvent implements DomainEvent {
    
    private String eventId;
    private String settlementId;
    private String orderId;
    private String storeId;
    private String distributorId;
    private Long settlementAmount;
    private LocalDateTime occurredOn;
    
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
