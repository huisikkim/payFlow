package com.example.payflow.catalog.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCartItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private OrderCart cart;
    
    @Column(nullable = false)
    private Long productId;  // 상품 ID
    
    @Column(nullable = false)
    private String productName;  // 상품명
    
    @Column(nullable = false)
    private Long unitPrice;  // 단가
    
    @Column(nullable = false)
    private String unit;  // 단위
    
    @Column(nullable = false)
    private Integer quantity;  // 수량
    
    @Column
    private Long subtotal;  // 소계 (단가 * 수량)
    
    @Column
    private String imageUrl;  // 상품 이미지
    
    @PrePersist
    @PreUpdate
    protected void calculateSubtotal() {
        this.subtotal = this.unitPrice * this.quantity;
    }
}
