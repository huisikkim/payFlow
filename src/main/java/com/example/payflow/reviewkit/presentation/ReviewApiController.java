package com.example.payflow.reviewkit.presentation;

import com.example.payflow.reviewkit.application.BusinessService;
import com.example.payflow.reviewkit.application.ReviewService;
import com.example.payflow.reviewkit.application.dto.ReviewResponse;
import com.example.payflow.reviewkit.application.dto.ReviewSubmitRequest;
import com.example.payflow.reviewkit.domain.Business;
import com.example.payflow.reviewkit.domain.Review;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviewkit")
@RequiredArgsConstructor
public class ReviewApiController {

    private final ReviewService reviewService;
    private final BusinessService businessService;

    /**
     * Public API: Submit a review (no authentication required)
     */
    @PostMapping("/businesses/{slug}/reviews")
    public ResponseEntity<Map<String, Object>> submitReview(
            @PathVariable String slug,
            @Valid @RequestBody ReviewSubmitRequest request) {
        
        Business business = businessService.getBusinessBySlug(slug);
        
        Review review = reviewService.submitReview(
                business.getId(),
                request.getReviewerName(),
                request.getReviewerEmail(),
                request.getRating(),
                request.getContent(),
                request.getReviewerCompany()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "리뷰가 성공적으로 제출되었습니다. 승인 후 공개됩니다.");
        response.put("reviewId", review.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * Dashboard API: Get all reviews for a business
     */
    @GetMapping("/businesses/{businessId}/reviews")
    public ResponseEntity<List<ReviewResponse>> getReviews(@PathVariable Long businessId) {
        List<Review> reviews = reviewService.getReviewsByBusiness(businessId);
        List<ReviewResponse> response = reviews.stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Dashboard API: Approve a review
     */
    @PostMapping("/reviews/{reviewId}/approve")
    public ResponseEntity<Map<String, Object>> approveReview(@PathVariable Long reviewId) {
        reviewService.approveReview(reviewId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "리뷰가 승인되었습니다.");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Dashboard API: Reject a review
     */
    @PostMapping("/reviews/{reviewId}/reject")
    public ResponseEntity<Map<String, Object>> rejectReview(@PathVariable Long reviewId) {
        reviewService.rejectReview(reviewId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "리뷰가 거부되었습니다.");
        
        return ResponseEntity.ok(response);
    }
}
