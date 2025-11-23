package com.example.payflow.invoice.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvoiceItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;
    
    @Column(nullable = false)
    private String itemName;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private Long unitPrice;
    
    @Column(nullable = false)
    private Long amount;
    
    private String unit;
    
    private LocalDateTime createdAt;
    
    public InvoiceItem(String itemName, Integer quantity, Long unitPrice, String unit) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = quantity * unitPrice;
        this.unit = unit;
        this.createdAt = LocalDateTime.now();
    }
    
    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }
}
