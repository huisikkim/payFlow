package com.example.payflow.catalog.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "distributor_order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributorOrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private DistributorOrder order;
    
    @Column(nullable = false)
    private Long productId;
    
    @Column(nullable = false)
    private String productName;
    
    @Column(nullable = false)
    private Long unitPrice;
    
    @Column(nullable = false)
    private String unit;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private Long subtotal; // 소계 (단가 × 수량)
    
    @Column
    private String imageUrl;
}
