package com.example.payflow.inventory.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String productId;
    
    @Column(nullable = false)
    private String productName;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private Integer reservedQuantity;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Inventory(String productId, String productName, Integer quantity) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.reservedQuantity = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean canReserve(Integer amount) {
        return (quantity - reservedQuantity) >= amount;
    }
    
    public void reserve(Integer amount) {
        if (!canReserve(amount)) {
            throw new IllegalStateException("재고가 부족합니다.");
        }
        this.reservedQuantity += amount;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void confirmReservation(Integer amount) {
        this.quantity -= amount;
        this.reservedQuantity -= amount;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void cancelReservation(Integer amount) {
        this.reservedQuantity -= amount;
        this.updatedAt = LocalDateTime.now();
    }
}
