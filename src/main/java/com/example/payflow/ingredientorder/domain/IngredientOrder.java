package com.example.payflow.ingredientorder.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ingredient_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IngredientOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String orderId;
    
    @Column(nullable = false)
    private String storeId;
    
    @Column(nullable = false)
    private String distributorId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IngredientOrderStatus status;
    
    @Column(nullable = false)
    private Long totalAmount;
    
    @OneToMany(mappedBy = "ingredientOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IngredientOrderItem> items = new ArrayList<>();
    
    private String rejectionReason;
    
    private LocalDateTime orderedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public IngredientOrder(String orderId, String storeId, String distributorId) {
        this.orderId = orderId;
        this.storeId = storeId;
        this.distributorId = distributorId;
        this.status = IngredientOrderStatus.PENDING;
        this.totalAmount = 0L;
        this.orderedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void addItem(IngredientOrderItem item) {
        items.add(item);
        item.setIngredientOrder(this);
        calculateTotalAmount();
    }
    
    public void confirm() {
        if (this.status != IngredientOrderStatus.PENDING) {
            throw new IllegalStateException("대기 중인 발주만 확인할 수 있습니다.");
        }
        this.status = IngredientOrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void reject(String reason) {
        if (this.status != IngredientOrderStatus.PENDING) {
            throw new IllegalStateException("대기 중인 발주만 거절할 수 있습니다.");
        }
        this.status = IngredientOrderStatus.REJECTED;
        this.rejectionReason = reason;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void complete() {
        if (this.status != IngredientOrderStatus.CONFIRMED) {
            throw new IllegalStateException("확인된 발주만 완료할 수 있습니다.");
        }
        this.status = IngredientOrderStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void cancel() {
        this.status = IngredientOrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateItemPrice(Long itemId, Long newPrice) {
        IngredientOrderItem item = items.stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("품목을 찾을 수 없습니다."));
        
        item.updatePrice(newPrice);
        calculateTotalAmount();
        this.updatedAt = LocalDateTime.now();
    }
    
    private void calculateTotalAmount() {
        this.totalAmount = items.stream()
            .mapToLong(IngredientOrderItem::getSubtotal)
            .sum();
    }
}
