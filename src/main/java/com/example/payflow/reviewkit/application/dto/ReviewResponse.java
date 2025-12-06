package com.example.payflow.reviewkit.application.dto;

import com.example.payflow.reviewkit.domain.Review;
import com.example.payflow.reviewkit.domain.ReviewStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {
    private Long id;
    private String reviewerName;
    private String reviewerCompany;
    private Integer rating;
    private String content;
    private String photoUrl;
    private String videoUrl;
    private ReviewStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .reviewerName(review.getReviewerName())
                .reviewerCompany(review.getReviewerCompany())
                .rating(review.getRating())
                .content(review.getContent())
                .photoUrl(review.getPhotoUrl())
                .videoUrl(review.getVideoUrl())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .approvedAt(review.getApprovedAt())
                .build();
    }
}
