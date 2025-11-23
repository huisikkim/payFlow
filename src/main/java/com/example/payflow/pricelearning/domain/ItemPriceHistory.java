package com.example.payflow.pricelearning.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "item_price_history", indexes = {
    @Index(name = "idx_item_name", columnList = "itemName"),
    @Index(name = "idx_recorded_at", columnList = "recordedAt")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemPriceHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String itemName;
    
    @Column(nullable = false)
    private Long unitPrice;
    
    @Column(nullable = false)
    private String unit;
    
    @Column(nullable = false)
    private String orderId;
    
    @Column(nullable = false)
    private String distributorId;
    
    @Column(nullable = false)
    private String storeId;
    
    @Column(nullable = false)
    private LocalDateTime recordedAt;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    public ItemPriceHistory(String itemName, Long unitPrice, String unit, 
                           String orderId, String distributorId, String storeId) {
        this.itemName = itemName;
        this.unitPrice = unitPrice;
        this.unit = unit;
        this.orderId = orderId;
        this.distributorId = distributorId;
        this.storeId = storeId;
        this.recordedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }
}
