package com.example.payflow.invoice.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String invoiceId;
    
    @Column(nullable = false)
    private String orderId;
    
    @Column(nullable = false)
    private String filePath;
    
    private String fileName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;
    
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();
    
    @Column(length = 1000)
    private String parseErrorMessage;
    
    private LocalDateTime uploadedAt;
    private LocalDateTime parsedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Invoice(String invoiceId, String orderId, String filePath, String fileName) {
        this.invoiceId = invoiceId;
        this.orderId = orderId;
        this.filePath = filePath;
        this.fileName = fileName;
        this.status = InvoiceStatus.UPLOADED;
        this.uploadedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void startParsing() {
        this.status = InvoiceStatus.PARSING;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void completeParsing() {
        this.status = InvoiceStatus.PARSED;
        this.parsedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void failParsing(String errorMessage) {
        this.status = InvoiceStatus.PARSE_FAILED;
        this.parseErrorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void addItem(InvoiceItem item) {
        items.add(item);
        item.setInvoice(this);
    }
    
    public void verify() {
        this.status = InvoiceStatus.VERIFIED;
        this.updatedAt = LocalDateTime.now();
    }
}
