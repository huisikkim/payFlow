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
public class SettlementCompletedEvent implements DomainEvent {
    
    private String eventId;
    private String settlementId;
    private String orderId;
    private String storeId;
    private Long paidAmount;
    private Long outstandingAmount;
    private LocalDateTime occurredOn;
    
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
