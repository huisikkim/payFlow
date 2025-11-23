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
public class InvoiceUploadedEvent implements DomainEvent {
    
    private String eventId;
    private String invoiceId;
    private String orderId;
    private String filePath;
    private String fileName;
    private LocalDateTime occurredOn;
    
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
