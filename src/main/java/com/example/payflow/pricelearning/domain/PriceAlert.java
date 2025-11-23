package com.example.payflow.pricelearning.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "price_alerts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PriceAlert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String alertId;
    
    @Column(nullable = false)
    private String itemName;
    
    @Column(nullable = false)
    private Long currentPrice;
    
    @Column(nullable = false)
    private Long averagePrice;
    
    @Column(nullable = false)
    private Double surgePercentage; // 급등률 (%)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriceAlertType alertType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriceAlertStatus status;
    
    @Column(nullable = false)
    private String orderId;
    
    @Column(nullable = false)
    private String distributorId;
    
    @Column(length = 500)
    private String message;
    
    private LocalDateTime acknowledgedAt;
    private LocalDateTime resolvedAt;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public PriceAlert(String alertId, String itemName, Long currentPrice, Long averagePrice,
                     String orderId, String distributorId, PriceAlertType alertType) {
        this.alertId = alertId;
        this.itemName = itemName;
        this.currentPrice = currentPrice;
        this.averagePrice = averagePrice;
        this.surgePercentage = calculateSurgePercentage(currentPrice, averagePrice);
        this.alertType = alertType;
        this.status = PriceAlertStatus.ACTIVE;
        this.orderId = orderId;
        this.distributorId = distributorId;
        this.message = generateMessage();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    private Double calculateSurgePercentage(Long current, Long average) {
        if (average == 0) return 0.0;
        return ((current - average) / (double) average) * 100;
    }
    
    private String generateMessage() {
        return String.format("%s의 단가가 평균 대비 %.1f%% 급등했습니다. (평균: %,d원 → 현재: %,d원)",
            itemName, surgePercentage, averagePrice, currentPrice);
    }
    
    public void acknowledge() {
        this.status = PriceAlertStatus.ACKNOWLEDGED;
        this.acknowledgedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void resolve() {
        this.status = PriceAlertStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
