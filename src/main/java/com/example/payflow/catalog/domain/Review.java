package com.example.payflow.catalog.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private DistributorOrder order;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewType reviewType; // STORE_TO_DISTRIBUTOR, DISTRIBUTOR_TO_STORE
    
    @Column(nullable = false)
    private String reviewerId; // 리뷰 작성자 ID
    
    @Column(nullable = false)
    private String reviewerName; // 리뷰 작성자 이름
    
    @Column(nullable = false)
    private String revieweeId; // 리뷰 대상자 ID
    
    @Column(nullable = false)
    private String revieweeName; // 리뷰 대상자 이름
    
    @Column(nullable = false)
    private Integer rating; // 평점 (1-5)
    
    @Column(length = 1000)
    private String comment; // 리뷰 내용
    
    // 세부 평점 (가게사장님 → 유통업자)
    @Column
    private Integer deliveryQuality; // 배송 품질 (1-5)
    
    @Column
    private Integer productQuality; // 상품 품질 (1-5)
    
    @Column
    private Integer serviceQuality; // 서비스 품질 (1-5)
    
    // 세부 평점 (유통업자 → 가게사장님)
    @Column
    private Integer paymentReliability; // 결제 신뢰도 (1-5)
    
    @Column
    private Integer communicationQuality; // 소통 품질 (1-5)
    
    @Column
    private Integer orderAccuracy; // 주문 정확도 (1-5)
    
    @Column(nullable = false)
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
    
    // 평점 유효성 검증
    public void validateRating() {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1-5 사이여야 합니다.");
        }
        
        if (reviewType == ReviewType.STORE_TO_DISTRIBUTOR) {
            if (deliveryQuality != null && (deliveryQuality < 1 || deliveryQuality > 5)) {
                throw new IllegalArgumentException("배송 품질 평점은 1-5 사이여야 합니다.");
            }
            if (productQuality != null && (productQuality < 1 || productQuality > 5)) {
                throw new IllegalArgumentException("상품 품질 평점은 1-5 사이여야 합니다.");
            }
            if (serviceQuality != null && (serviceQuality < 1 || serviceQuality > 5)) {
                throw new IllegalArgumentException("서비스 품질 평점은 1-5 사이여야 합니다.");
            }
        } else if (reviewType == ReviewType.DISTRIBUTOR_TO_STORE) {
            if (paymentReliability != null && (paymentReliability < 1 || paymentReliability > 5)) {
                throw new IllegalArgumentException("결제 신뢰도 평점은 1-5 사이여야 합니다.");
            }
            if (communicationQuality != null && (communicationQuality < 1 || communicationQuality > 5)) {
                throw new IllegalArgumentException("소통 품질 평점은 1-5 사이여야 합니다.");
            }
            if (orderAccuracy != null && (orderAccuracy < 1 || orderAccuracy > 5)) {
                throw new IllegalArgumentException("주문 정확도 평점은 1-5 사이여야 합니다.");
            }
        }
    }
}
