package com.example.payflow.parlevel.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "predicted_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PredictedOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String storeId;
    
    @Column(nullable = false)
    private String itemName;
    
    @Column(nullable = false)
    private Integer currentStock;
    
    @Column(nullable = false)
    private Integer predictedConsumption;  // 예상 소비량
    
    @Column(nullable = false)
    private Integer recommendedOrderQuantity;
    
    @Column(nullable = false)
    private LocalDate predictedOrderDate;
    
    @Column(nullable = false)
    private LocalDate expectedDeliveryDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PredictionStatus status;
    
    private String actualOrderId;  // 실제 발주 ID
    
    private String reason;  // 예측 근거
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public PredictedOrder(String storeId, String itemName, Integer currentStock,
                         Integer predictedConsumption, Integer recommendedOrderQuantity,
                         LocalDate predictedOrderDate, LocalDate expectedDeliveryDate,
                         String reason) {
        this.storeId = storeId;
        this.itemName = itemName;
        this.currentStock = currentStock;
        this.predictedConsumption = predictedConsumption;
        this.recommendedOrderQuantity = recommendedOrderQuantity;
        this.predictedOrderDate = predictedOrderDate;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.status = PredictionStatus.PENDING;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsOrdered(String orderId) {
        this.status = PredictionStatus.ORDERED;
        this.actualOrderId = orderId;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsSkipped() {
        this.status = PredictionStatus.SKIPPED;
        this.updatedAt = LocalDateTime.now();
    }
}
