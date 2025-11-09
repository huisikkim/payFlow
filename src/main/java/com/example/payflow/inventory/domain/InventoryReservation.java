package com.example.payflow.inventory.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryReservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String orderId;
    
    @Column(nullable = false)
    private String productId;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public InventoryReservation(String orderId, String productId, Integer quantity) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = ReservationStatus.RESERVED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
}
