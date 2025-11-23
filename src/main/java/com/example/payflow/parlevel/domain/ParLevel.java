package com.example.payflow.parlevel.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "par_levels", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"storeId", "itemName"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParLevel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String storeId;
    
    @Column(nullable = false)
    private String itemName;
    
    @Column(nullable = false)
    private String unit;
    
    @Column(nullable = false)
    private Integer minLevel;  // 최소 재고 수준
    
    @Column(nullable = false)
    private Integer maxLevel;  // 최대 재고 수준
    
    @Column(nullable = false)
    private Integer safetyStock;  // 안전 재고
    
    @Column(nullable = false)
    private Integer leadTimeDays;  // 리드타임 (일)
    
    @Column(nullable = false)
    private Boolean autoOrderEnabled;  // 자동 발주 활성화
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public ParLevel(String storeId, String itemName, String unit,
                   Integer minLevel, Integer maxLevel, Integer safetyStock,
                   Integer leadTimeDays, Boolean autoOrderEnabled) {
        this.storeId = storeId;
        this.itemName = itemName;
        this.unit = unit;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.safetyStock = safetyStock;
        this.leadTimeDays = leadTimeDays;
        this.autoOrderEnabled = autoOrderEnabled;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateLevels(Integer minLevel, Integer maxLevel, Integer safetyStock) {
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.safetyStock = safetyStock;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateLeadTime(Integer leadTimeDays) {
        this.leadTimeDays = leadTimeDays;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void enableAutoOrder() {
        this.autoOrderEnabled = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void disableAutoOrder() {
        this.autoOrderEnabled = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean needsReorder(Integer currentStock) {
        return currentStock <= minLevel;
    }
    
    public Integer calculateOrderQuantity(Integer currentStock) {
        return Math.max(0, maxLevel - currentStock);
    }
}
