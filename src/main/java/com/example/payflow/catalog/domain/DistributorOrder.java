package com.example.payflow.catalog.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "distributor_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributorOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String storeId;
    
    @Column(nullable = false)
    private String distributorId;
    
    @Column(nullable = false)
    private String orderNumber; // 주문번호 (자동생성)
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DistributorOrderItem> items = new ArrayList<>();
    
    @Column(nullable = false)
    private Long totalAmount; // 총 주문 금액
    
    @Column(nullable = false)
    private Integer totalQuantity; // 총 주문 수량
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column
    private String deliveryAddress;
    
    @Column
    private String deliveryPhone;
    
    @Column
    private String deliveryRequest; // 배송 요청사항
    
    @Column
    private LocalDateTime desiredDeliveryDate; // 희망 배송일
    
    @Column(nullable = false)
    private LocalDateTime orderedAt;
    
    @Column
    private LocalDateTime confirmedAt;
    
    @Column
    private LocalDateTime shippedAt;
    
    @Column
    private LocalDateTime deliveredAt;
    
    @Column
    private LocalDateTime cancelledAt;
    
    @Column
    private String cancellationReason;
    
    public void addItem(DistributorOrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
    
    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }
    
    public void ship() {
        this.status = OrderStatus.SHIPPED;
        this.shippedAt = LocalDateTime.now();
    }
    
    public void deliver() {
        this.status = OrderStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }
    
    public void cancel(String reason) {
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }
}
