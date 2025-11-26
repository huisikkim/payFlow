package com.example.payflow.catalog.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_catalogs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCatalog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String distributorId;  // 유통업체 ID
    
    @Column(nullable = false)
    private String productName;  // 상품명
    
    @Column(nullable = false)
    private String category;  // 카테고리 (쌀/곡물, 채소, 과일, 육류, 수산물, 유제품 등)
    
    @Column(columnDefinition = "TEXT")
    private String description;  // 상품 설명
    
    @Column(nullable = false)
    private Long unitPrice;  // 단가
    
    @Column(nullable = false)
    private String unit;  // 단위 (kg, 개, 박스 등)
    
    @Column
    private Integer stockQuantity;  // 재고 수량
    
    @Column
    private String origin;  // 원산지
    
    @Column
    private String brand;  // 브랜드
    
    @Column
    private String imageUrl;  // 상품 이미지 URL
    
    @Column
    private Boolean isAvailable;  // 판매 가능 여부
    
    @Column
    private Integer minOrderQuantity;  // 최소 주문 수량
    
    @Column
    private Integer maxOrderQuantity;  // 최대 주문 수량
    
    @Column
    private String certifications;  // 인증 정보 (유기농, HACCP 등)
    
    @Column
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isAvailable == null) {
            isAvailable = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 재고 차감
     */
    public void decreaseStock(int quantity) {
        if (stockQuantity == null) {
            throw new IllegalStateException("재고 정보가 없습니다");
        }
        if (stockQuantity < quantity) {
            throw new IllegalStateException("재고가 부족합니다");
        }
        this.stockQuantity -= quantity;
    }
    
    /**
     * 재고 증가
     */
    public void increaseStock(int quantity) {
        if (stockQuantity == null) {
            this.stockQuantity = quantity;
        } else {
            this.stockQuantity += quantity;
        }
    }
    
    /**
     * 판매 가능 여부 확인
     */
    public boolean canOrder(int quantity) {
        if (!isAvailable) {
            return false;
        }
        if (minOrderQuantity != null && quantity < minOrderQuantity) {
            return false;
        }
        if (maxOrderQuantity != null && quantity > maxOrderQuantity) {
            return false;
        }
        if (stockQuantity != null && stockQuantity < quantity) {
            return false;
        }
        return true;
    }
}
