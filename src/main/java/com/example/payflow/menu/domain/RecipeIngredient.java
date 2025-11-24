package com.example.payflow.menu.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recipe_ingredients")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecipeIngredient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;
    
    @Column(nullable = false)
    private String ingredientName; // 재료명
    
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity; // 사용량
    
    @Column(nullable = false)
    private String unit; // 단위 (kg, g, 개 등)
    
    @Column(length = 500)
    private String notes; // 비고 (예: 손질 방법, 대체 재료 등)
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    public RecipeIngredient(String ingredientName, BigDecimal quantity, String unit, String notes) {
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setMenu(Menu menu) {
        this.menu = menu;
    }
    
    public void updateQuantity(BigDecimal newQuantity) {
        this.quantity = newQuantity;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateInfo(String ingredientName, BigDecimal quantity, String unit, String notes) {
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }
}
