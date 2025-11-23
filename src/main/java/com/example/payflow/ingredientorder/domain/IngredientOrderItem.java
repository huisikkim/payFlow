package com.example.payflow.ingredientorder.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ingredient_order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IngredientOrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_order_id", nullable = false)
    private IngredientOrder ingredientOrder;
    
    @Column(nullable = false)
    private String itemName;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private Long unitPrice;
    
    @Column(nullable = false)
    private Long subtotal;
    
    private String unit; // 단위 (kg, 개, 박스 등)
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public IngredientOrderItem(String itemName, Integer quantity, Long unitPrice, String unit) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.unit = unit;
        this.subtotal = quantity * unitPrice;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setIngredientOrder(IngredientOrder ingredientOrder) {
        this.ingredientOrder = ingredientOrder;
    }
    
    public void updatePrice(Long newPrice) {
        this.unitPrice = newPrice;
        this.subtotal = this.quantity * newPrice;
        this.updatedAt = LocalDateTime.now();
    }
}
