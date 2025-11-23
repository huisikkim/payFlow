package com.example.payflow.invoice.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class InvoiceUploadedEvent implements DomainEvent {
    
    private final String eventId;
    private final String invoiceId;
    private final String orderId;
    private final String filePath;
    private final String fileName;
    private final LocalDateTime occurredOn;
    
    public InvoiceUploadedEvent(String invoiceId, String orderId, String filePath, String fileName) {
        this.eventId = UUID.randomUUID().toString();
        this.invoiceId = invoiceId;
        this.orderId = orderId;
        this.filePath = filePath;
        this.fileName = fileName;
        this.occurredOn = LocalDateTime.now();
    }
    
    @Override
    public String getEventType() {
        return "InvoiceUploaded";
    }
}
