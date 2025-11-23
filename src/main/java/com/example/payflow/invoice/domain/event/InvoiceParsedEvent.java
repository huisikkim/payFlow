package com.example.payflow.invoice.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceParsedEvent implements DomainEvent {
    
    private String eventId;
    private String invoiceId;
    private String orderId;
    private Integer itemCount;
    private Long totalAmount;
    private LocalDateTime occurredOn;
    
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
