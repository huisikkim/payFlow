package com.example.payflow.catalog.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String storeId;  // 매장 ID
    
    @Column(nullable = false)
    private String distributorId;  // 유통업체 ID
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderCartItem> items = new ArrayList<>();
    
    @Column
    private Long totalAmount;  // 총 금액
    
    @Column
    private Integer totalQuantity;  // 총 수량
    
    @Column
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 장바구니 아이템 추가
     */
    public void addItem(OrderCartItem item) {
        items.add(item);
        item.setCart(this);
        calculateTotal();
    }
    
    /**
     * 장바구니 아이템 제거
     */
    public void removeItem(OrderCartItem item) {
        items.remove(item);
        item.setCart(null);
        calculateTotal();
    }
    
    /**
     * 총액 계산
     */
    public void calculateTotal() {
        this.totalAmount = items.stream()
                .mapToLong(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
        this.totalQuantity = items.stream()
                .mapToInt(OrderCartItem::getQuantity)
                .sum();
    }
    
    /**
     * 장바구니 비우기
     */
    public void clear() {
        items.clear();
        totalAmount = 0L;
        totalQuantity = 0;
    }
}
