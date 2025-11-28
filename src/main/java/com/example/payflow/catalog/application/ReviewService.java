package com.example.payflow.catalog.application;

import com.example.payflow.catalog.domain.*;
import com.example.payflow.catalog.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final DistributorOrderRepository orderRepository;
    
    /**
     * 리뷰 작성 (가게사장님 → 유통업자)
     */
    @Transactional
    public ReviewResponse createStoreReview(String storeId, CreateReviewRequest request) {
        DistributorOrder order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        // 권한 확인
        if (!order.getStoreId().equals(storeId)) {
            throw new IllegalArgumentException("본인의 주문에만 리뷰를 작성할 수 있습니다.");
        }
        
        // 배송 완료 확인
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("배송 완료된 주문만 리뷰를 작성할 수 있습니다.");
        }
        
        // 중복 리뷰 확인
        if (reviewRepository.existsByOrderIdAndReviewType(order.getId(), ReviewType.STORE_TO_DISTRIBUTOR)) {
            throw new IllegalArgumentException("이미 리뷰를 작성한 주문입니다.");
        }
        
        Review review = Review.builder()
                .order(order)
                .reviewType(ReviewType.STORE_TO_DISTRIBUTOR)
                .reviewerId(storeId)
                .reviewerName("매장-" + storeId)
                .revieweeId(order.getDistributorId())
                .revieweeName("유통업체-" + order.getDistributorId())
                .rating(request.getRating())
                .comment(request.getComment())
                .deliveryQuality(request.getDeliveryQuality())
                .productQuality(request.getProductQuality())
                .serviceQuality(request.getServiceQuality())
                .build();
        
        review.validateRating();
        
        Review saved = reviewRepository.save(review);
        
        log.info("가게사장님 리뷰 작성: 주문번호={}, 평점={}", order.getOrderNumber(), request.getRating());
        
        return toReviewResponse(saved);
    }
    
    /**
     * 리뷰 작성 (유통업자 → 가게사장님)
     */
    @Transactional
    public ReviewResponse createDistributorReview(String distributorId, CreateReviewRequest request) {
        DistributorOrder order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        // 권한 확인
        if (!order.getDistributorId().equals(distributorId)) {
            throw new IllegalArgumentException("본인의 주문에만 리뷰를 작성할 수 있습니다.");
        }
        
        // 배송 완료 확인
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("배송 완료된 주문만 리뷰를 작성할 수 있습니다.");
        }
        
        // 중복 리뷰 확인
        if (reviewRepository.existsByOrderIdAndReviewType(order.getId(), ReviewType.DISTRIBUTOR_TO_STORE)) {
            throw new IllegalArgumentException("이미 리뷰를 작성한 주문입니다.");
        }
        
        Review review = Review.builder()
                .order(order)
                .reviewType(ReviewType.DISTRIBUTOR_TO_STORE)
                .reviewerId(distributorId)
                .reviewerName("유통업체-" + distributorId)
                .revieweeId(order.getStoreId())
                .revieweeName("매장-" + order.getStoreId())
                .rating(request.getRating())
                .comment(request.getComment())
                .paymentReliability(request.getPaymentReliability())
                .communicationQuality(request.getCommunicationQuality())
                .orderAccuracy(request.getOrderAccuracy())
                .build();
        
        review.validateRating();
        
        Review saved = reviewRepository.save(review);
        
        log.info("유통업자 리뷰 작성: 주문번호={}, 평점={}", order.getOrderNumber(), request.getRating());
        
        return toReviewResponse(saved);
    }
    
    /**
     * 내가 받은 리뷰 조회
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReceivedReviews(String userId) {
        List<Review> reviews = reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(userId);
        return reviews.stream()
                .map(this::toReviewResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 내가 작성한 리뷰 조회
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getWrittenReviews(String userId) {
        List<Review> reviews = reviewRepository.findByReviewerIdOrderByCreatedAtDesc(userId);
        return reviews.stream()
                .map(this::toReviewResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 주문별 리뷰 조회
     */
    @Transactional(readOnly = true)
    public ReviewResponse getReviewByOrder(Long orderId, ReviewType reviewType) {
        Review review = reviewRepository.findByOrderIdAndReviewType(orderId, reviewType)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        
        return toReviewResponse(review);
    }
    
    /**
     * 리뷰 통계 조회
     */
    @Transactional(readOnly = true)
    public ReviewStatisticsResponse getReviewStatistics(String userId) {
        List<Review> reviews = reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(userId);
        
        if (reviews.isEmpty()) {
            return ReviewStatisticsResponse.builder()
                    .userId(userId)
                    .averageRating(0.0)
                    .totalReviews(0L)
                    .rating5Count(0L)
                    .rating4Count(0L)
                    .rating3Count(0L)
                    .rating2Count(0L)
                    .rating1Count(0L)
                    .build();
        }
        
        Double avgRating = reviewRepository.calculateAverageRating(userId);
        Long totalReviews = reviewRepository.countByRevieweeId(userId);
        
        // 평점별 개수
        Long rating5 = reviews.stream().filter(r -> r.getRating() == 5).count();
        Long rating4 = reviews.stream().filter(r -> r.getRating() == 4).count();
        Long rating3 = reviews.stream().filter(r -> r.getRating() == 3).count();
        Long rating2 = reviews.stream().filter(r -> r.getRating() == 2).count();
        Long rating1 = reviews.stream().filter(r -> r.getRating() == 1).count();
        
        // 세부 평점 평균
        Double avgDelivery = reviews.stream()
                .filter(r -> r.getDeliveryQuality() != null)
                .mapToInt(Review::getDeliveryQuality)
                .average().orElse(0.0);
        
        Double avgProduct = reviews.stream()
                .filter(r -> r.getProductQuality() != null)
                .mapToInt(Review::getProductQuality)
                .average().orElse(0.0);
        
        Double avgService = reviews.stream()
                .filter(r -> r.getServiceQuality() != null)
                .mapToInt(Review::getServiceQuality)
                .average().orElse(0.0);
        
        Double avgPayment = reviews.stream()
                .filter(r -> r.getPaymentReliability() != null)
                .mapToInt(Review::getPaymentReliability)
                .average().orElse(0.0);
        
        Double avgCommunication = reviews.stream()
                .filter(r -> r.getCommunicationQuality() != null)
                .mapToInt(Review::getCommunicationQuality)
                .average().orElse(0.0);
        
        Double avgOrder = reviews.stream()
                .filter(r -> r.getOrderAccuracy() != null)
                .mapToInt(Review::getOrderAccuracy)
                .average().orElse(0.0);
        
        return ReviewStatisticsResponse.builder()
                .userId(userId)
                .averageRating(avgRating)
                .totalReviews(totalReviews)
                .rating5Count(rating5)
                .rating4Count(rating4)
                .rating3Count(rating3)
                .rating2Count(rating2)
                .rating1Count(rating1)
                .avgDeliveryQuality(avgDelivery > 0 ? avgDelivery : null)
                .avgProductQuality(avgProduct > 0 ? avgProduct : null)
                .avgServiceQuality(avgService > 0 ? avgService : null)
                .avgPaymentReliability(avgPayment > 0 ? avgPayment : null)
                .avgCommunicationQuality(avgCommunication > 0 ? avgCommunication : null)
                .avgOrderAccuracy(avgOrder > 0 ? avgOrder : null)
                .build();
    }
    
    /**
     * Entity -> Response 변환
     */
    private ReviewResponse toReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .orderId(review.getOrder().getId())
                .orderNumber(review.getOrder().getOrderNumber())
                .reviewType(review.getReviewType())
                .reviewTypeDescription(review.getReviewType().getDescription())
                .reviewerId(review.getReviewerId())
                .reviewerName(review.getReviewerName())
                .revieweeId(review.getRevieweeId())
                .revieweeName(review.getRevieweeName())
                .rating(review.getRating())
                .comment(review.getComment())
                .deliveryQuality(review.getDeliveryQuality())
                .productQuality(review.getProductQuality())
                .serviceQuality(review.getServiceQuality())
                .paymentReliability(review.getPaymentReliability())
                .communicationQuality(review.getCommunicationQuality())
                .orderAccuracy(review.getOrderAccuracy())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
