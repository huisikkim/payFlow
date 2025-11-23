package com.example.payflow.invoice.presentation.dto;

import com.example.payflow.invoice.domain.Invoice;
import com.example.payflow.invoice.domain.InvoiceStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InvoiceUploadResponse {
    
    private final String invoiceId;
    private final String orderId;
    private final String fileName;
    private final InvoiceStatus status;
    private final LocalDateTime uploadedAt;
    
    public InvoiceUploadResponse(String invoiceId, String orderId, String fileName, 
                                InvoiceStatus status, LocalDateTime uploadedAt) {
        this.invoiceId = invoiceId;
        this.orderId = orderId;
        this.fileName = fileName;
        this.status = status;
        this.uploadedAt = uploadedAt;
    }
    
    public static InvoiceUploadResponse from(Invoice invoice) {
        return new InvoiceUploadResponse(
            invoice.getInvoiceId(),
            invoice.getOrderId(),
            invoice.getFileName(),
            invoice.getStatus(),
            invoice.getUploadedAt()
        );
    }
}
