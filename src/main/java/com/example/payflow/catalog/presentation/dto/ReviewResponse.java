package com.example.payflow.catalog.presentation.dto;

import com.example.payflow.catalog.domain.ReviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    
    private Long id;
    
    private Long orderId;
    
    private String orderNumber;
    
    private ReviewType reviewType;
    
    private String reviewTypeDescription;
    
    private String reviewerId;
    
    private String reviewerName;
    
    private String revieweeId;
    
    private String revieweeName;
    
    private Integer rating;
    
    private String comment;
    
    // 세부 평점 (가게사장님 → 유통업자)
    private Integer deliveryQuality;
    private Integer productQuality;
    private Integer serviceQuality;
    
    // 세부 평점 (유통업자 → 가게사장님)
    private Integer paymentReliability;
    private Integer communicationQuality;
    private Integer orderAccuracy;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
