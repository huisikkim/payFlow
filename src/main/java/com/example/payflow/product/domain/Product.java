package com.example.payflow.product.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 2000)
    private String description;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCondition productCondition;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;
    
    @Column(nullable = false)
    private Long sellerId;
    
    @Column
    private String sellerName;
    
    @Column
    private String location;
    
    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();
    
    @Column
    private Integer viewCount = 0;
    
    @Column
    private Integer likeCount = 0;
    
    @Column
    private Integer chatCount = 0;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @Column
    private LocalDateTime soldAt;
    
    // 생성자
    public Product(String title, String description, BigDecimal price, 
                   ProductCategory category, ProductCondition productCondition,
                   Long sellerId, String sellerName, String location, List<String> imageUrls) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.productCondition = productCondition;
        this.status = ProductStatus.AVAILABLE;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.location = location;
        this.imageUrls = imageUrls != null ? new ArrayList<>(imageUrls) : new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.viewCount = 0;
        this.likeCount = 0;
        this.chatCount = 0;
    }
    
    // 비즈니스 메서드
    public void updateProduct(String title, String description, BigDecimal price,
                             ProductCategory category, ProductCondition productCondition,
                             String location, List<String> imageUrls) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.productCondition = productCondition;
        this.location = location;
        if (imageUrls != null) {
            this.imageUrls = new ArrayList<>(imageUrls);
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsSold() {
        this.status = ProductStatus.SOLD;
        this.soldAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void reserve() {
        if (this.status != ProductStatus.AVAILABLE) {
            throw new IllegalStateException("상품을 예약할 수 없는 상태입니다.");
        }
        this.status = ProductStatus.RESERVED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void cancelReservation() {
        if (this.status != ProductStatus.RESERVED) {
            throw new IllegalStateException("예약 상태가 아닙니다.");
        }
        this.status = ProductStatus.AVAILABLE;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void hide() {
        this.status = ProductStatus.HIDDEN;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void show() {
        if (this.status == ProductStatus.HIDDEN) {
            this.status = ProductStatus.AVAILABLE;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    public void delete() {
        this.status = ProductStatus.DELETED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void incrementViewCount() {
        this.viewCount++;
    }
    
    public void incrementLikeCount() {
        this.likeCount++;
    }
    
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
    
    public void incrementChatCount() {
        this.chatCount++;
    }
}
