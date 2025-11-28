package com.example.payflow.catalog.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewStatisticsResponse {
    
    private String userId;
    
    private String userName;
    
    private Double averageRating; // 전체 평균 평점
    
    private Long totalReviews; // 총 리뷰 개수
    
    // 평점별 개수
    private Long rating5Count;
    private Long rating4Count;
    private Long rating3Count;
    private Long rating2Count;
    private Long rating1Count;
    
    // 세부 평점 평균 (가게사장님 → 유통업자)
    private Double avgDeliveryQuality;
    private Double avgProductQuality;
    private Double avgServiceQuality;
    
    // 세부 평점 평균 (유통업자 → 가게사장님)
    private Double avgPaymentReliability;
    private Double avgCommunicationQuality;
    private Double avgOrderAccuracy;
}
