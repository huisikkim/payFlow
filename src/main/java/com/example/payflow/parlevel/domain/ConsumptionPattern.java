package com.example.payflow.parlevel.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "consumption_patterns", indexes = {
    @Index(name = "idx_store_item", columnList = "storeId,itemName"),
    @Index(name = "idx_date", columnList = "consumptionDate")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsumptionPattern {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String storeId;
    
    @Column(nullable = false)
    private String itemName;
    
    @Column(nullable = false)
    private LocalDate consumptionDate;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private String unit;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;
    
    @Column(nullable = false)
    private Integer weekOfMonth;
    
    @Column(name = "month_of_year", nullable = false)
    private Integer month;
    
    private LocalDateTime createdAt;
    
    public ConsumptionPattern(String storeId, String itemName, LocalDate consumptionDate,
                             Integer quantity, String unit) {
        this.storeId = storeId;
        this.itemName = itemName;
        this.consumptionDate = consumptionDate;
        this.quantity = quantity;
        this.unit = unit;
        this.dayOfWeek = consumptionDate.getDayOfWeek();
        this.weekOfMonth = (consumptionDate.getDayOfMonth() - 1) / 7 + 1;
        this.month = consumptionDate.getMonthValue();
        this.createdAt = LocalDateTime.now();
    }
}
