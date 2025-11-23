package com.example.payflow.invoice.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class InvoiceParsedEvent implements DomainEvent {
    
    private final String eventId;
    private final String invoiceId;
    private final String orderId;
    private final Integer itemCount;
    private final Long totalAmount;
    private final LocalDateTime occurredOn;
    
    public InvoiceParsedEvent(String invoiceId, String orderId, Integer itemCount, Long totalAmount) {
        this.eventId = UUID.randomUUID().toString();
        this.invoiceId = invoiceId;
        this.orderId = orderId;
        this.itemCount = itemCount;
        this.totalAmount = totalAmount;
        this.occurredOn = LocalDateTime.now();
    }
    
    @Override
    public String getEventType() {
        return "InvoiceParsed";
    }
}
