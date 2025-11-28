package com.example.payflow.catalog.presentation;

import com.example.payflow.catalog.application.ReviewService;
import com.example.payflow.catalog.domain.ReviewType;
import com.example.payflow.catalog.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {
    
    private final ReviewService reviewService;
    
    /**
     * 리뷰 작성 (가게사장님 → 유통업자)
     * POST /api/reviews/store
     */
    @PostMapping("/store")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<ReviewResponse> createStoreReview(@RequestBody CreateReviewRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String storeId = authentication.getName();
        
        log.info("가게사장님 리뷰 작성 요청: 매장={}, 주문ID={}", storeId, request.getOrderId());
        
        try {
            ReviewResponse review = reviewService.createStoreReview(storeId, request);
            return ResponseEntity.ok(review);
        } catch (IllegalArgumentException e) {
            log.error("리뷰 작성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 리뷰 작성 (유통업자 → 가게사장님)
     * POST /api/reviews/distributor
     */
    @PostMapping("/distributor")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public ResponseEntity<ReviewResponse> createDistributorReview(@RequestBody CreateReviewRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String distributorId = authentication.getName();
        
        log.info("유통업자 리뷰 작성 요청: 유통업체={}, 주문ID={}", distributorId, request.getOrderId());
        
        try {
            ReviewResponse review = reviewService.createDistributorReview(distributorId, request);
            return ResponseEntity.ok(review);
        } catch (IllegalArgumentException e) {
            log.error("리뷰 작성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 내가 받은 리뷰 조회
     * GET /api/reviews/received
     */
    @GetMapping("/received")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'DISTRIBUTOR')")
    public ResponseEntity<List<ReviewResponse>> getReceivedReviews() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        
        log.info("받은 리뷰 조회: 사용자={}", userId);
        
        List<ReviewResponse> reviews = reviewService.getReceivedReviews(userId);
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * 내가 작성한 리뷰 조회
     * GET /api/reviews/written
     */
    @GetMapping("/written")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'DISTRIBUTOR')")
    public ResponseEntity<List<ReviewResponse>> getWrittenReviews() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        
        log.info("작성한 리뷰 조회: 사용자={}", userId);
        
        List<ReviewResponse> reviews = reviewService.getWrittenReviews(userId);
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * 주문별 리뷰 조회
     * GET /api/reviews/order/{orderId}?type=STORE_TO_DISTRIBUTOR
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'DISTRIBUTOR')")
    public ResponseEntity<ReviewResponse> getReviewByOrder(
            @PathVariable Long orderId,
            @RequestParam ReviewType type) {
        
        log.info("주문별 리뷰 조회: 주문ID={}, 타입={}", orderId, type);
        
        try {
            ReviewResponse review = reviewService.getReviewByOrder(orderId, type);
            return ResponseEntity.ok(review);
        } catch (IllegalArgumentException e) {
            log.error("리뷰 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 리뷰 통계 조회
     * GET /api/reviews/statistics/{userId}
     */
    @GetMapping("/statistics/{userId}")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'DISTRIBUTOR')")
    public ResponseEntity<ReviewStatisticsResponse> getReviewStatistics(@PathVariable String userId) {
        log.info("리뷰 통계 조회: 사용자={}", userId);
        
        ReviewStatisticsResponse statistics = reviewService.getReviewStatistics(userId);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 내 리뷰 통계 조회
     * GET /api/reviews/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'DISTRIBUTOR')")
    public ResponseEntity<ReviewStatisticsResponse> getMyReviewStatistics() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        
        log.info("내 리뷰 통계 조회: 사용자={}", userId);
        
        ReviewStatisticsResponse statistics = reviewService.getReviewStatistics(userId);
        return ResponseEntity.ok(statistics);
    }
}
