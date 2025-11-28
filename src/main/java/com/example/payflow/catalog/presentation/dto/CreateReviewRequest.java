package com.example.payflow.catalog.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReviewRequest {
    
    private Long orderId; // 주문 ID
    
    private Integer rating; // 전체 평점 (1-5, 필수)
    
    private String comment; // 리뷰 내용 (선택)
    
    // 가게사장님 → 유통업자 리뷰 시
    private Integer deliveryQuality; // 배송 품질 (1-5, 선택)
    private Integer productQuality; // 상품 품질 (1-5, 선택)
    private Integer serviceQuality; // 서비스 품질 (1-5, 선택)
    
    // 유통업자 → 가게사장님 리뷰 시
    private Integer paymentReliability; // 결제 신뢰도 (1-5, 선택)
    private Integer communicationQuality; // 소통 품질 (1-5, 선택)
    private Integer orderAccuracy; // 주문 정확도 (1-5, 선택)
}
