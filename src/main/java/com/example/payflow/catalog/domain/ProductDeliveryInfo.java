package com.example.payflow.catalog.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_delivery_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDeliveryInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long productId;  // 상품 ID
    
    @Column
    private String deliveryType;  // 배송 타입 (당일배송, 익일배송, 일반배송)
    
    @Column
    private Integer deliveryFee;  // 배송비
    
    @Column
    private Integer freeDeliveryThreshold;  // 무료 배송 기준 금액
    
    @Column
    private String deliveryRegions;  // 배송 가능 지역 (콤마 구분)
    
    @Column
    private String deliveryDays;  // 배송 가능 요일 (월,화,수,목,금,토,일)
    
    @Column
    private String deliveryTimeSlots;  // 배송 시간대 (오전, 오후, 저녁)
    
    @Column
    private Integer estimatedDeliveryDays;  // 예상 배송 일수
    
    @Column
    private String packagingType;  // 포장 타입 (박스, 비닐, 냉장, 냉동)
    
    @Column
    private Boolean isFragile;  // 파손 주의 여부
    
    @Column
    private Boolean requiresRefrigeration;  // 냉장 보관 필요 여부
    
    @Column
    private String specialInstructions;  // 특별 배송 지시사항
    
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
}
