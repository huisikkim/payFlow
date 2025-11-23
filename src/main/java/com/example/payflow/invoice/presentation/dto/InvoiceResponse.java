package com.example.payflow.invoice.presentation.dto;

import com.example.payflow.invoice.domain.Invoice;
import com.example.payflow.invoice.domain.InvoiceItem;
import com.example.payflow.invoice.domain.InvoiceStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class InvoiceResponse {
    
    private final String invoiceId;
    private final String orderId;
    private final String fileName;
    private final InvoiceStatus status;
    private final List<InvoiceItemDto> items;
    private final String parseErrorMessage;
    private final LocalDateTime uploadedAt;
    private final LocalDateTime parsedAt;
    
    public InvoiceResponse(String invoiceId, String orderId, String fileName, InvoiceStatus status,
                          List<InvoiceItemDto> items, String parseErrorMessage,
                          LocalDateTime uploadedAt, LocalDateTime parsedAt) {
        this.invoiceId = invoiceId;
        this.orderId = orderId;
        this.fileName = fileName;
        this.status = status;
        this.items = items;
        this.parseErrorMessage = parseErrorMessage;
        this.uploadedAt = uploadedAt;
        this.parsedAt = parsedAt;
    }
    
    public static InvoiceResponse from(Invoice invoice) {
        List<InvoiceItemDto> items = invoice.getItems().stream()
            .map(InvoiceItemDto::from)
            .collect(Collectors.toList());
        
        return new InvoiceResponse(
            invoice.getInvoiceId(),
            invoice.getOrderId(),
            invoice.getFileName(),
            invoice.getStatus(),
            items,
            invoice.getParseErrorMessage(),
            invoice.getUploadedAt(),
            invoice.getParsedAt()
        );
    }
}
