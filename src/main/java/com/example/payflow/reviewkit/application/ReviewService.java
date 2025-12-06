package com.example.payflow.reviewkit.application;

import com.example.payflow.reviewkit.domain.Business;
import com.example.payflow.reviewkit.domain.Review;
import com.example.payflow.reviewkit.domain.ReviewStatus;
import com.example.payflow.reviewkit.infrastructure.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("reviewKitReviewService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BusinessService businessService;

    @Transactional
    public Review submitReview(Long businessId, String reviewerName, String reviewerEmail, 
                               Integer rating, String content, String reviewerCompany) {
        Business business = businessService.getBusinessById(businessId);

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Review review = Review.builder()
                .business(business)
                .reviewerName(reviewerName)
                .reviewerEmail(reviewerEmail)
                .reviewerCompany(reviewerCompany)
                .rating(rating)
                .content(content)
                .status(ReviewStatus.PENDING)
                .build();

        return reviewRepository.save(review);
    }

    @Transactional
    public void approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));
        review.approve();
        reviewRepository.save(review);
    }

    @Transactional
    public void rejectReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));
        review.reject();
        reviewRepository.save(review);
    }

    public List<Review> getReviewsByBusiness(Long businessId) {
        return reviewRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);
    }

    public List<Review> getApprovedReviews(Long businessId) {
        return reviewRepository.findByBusinessIdAndStatusOrderByCreatedAtDesc(businessId, ReviewStatus.APPROVED);
    }

    public List<Review> getPendingReviews(Long businessId) {
        return reviewRepository.findByBusinessIdAndStatusOrderByCreatedAtDesc(businessId, ReviewStatus.PENDING);
    }

    public long countApprovedReviews(Long businessId) {
        return reviewRepository.countByBusinessIdAndStatus(businessId, ReviewStatus.APPROVED);
    }

    public long countPendingReviews(Long businessId) {
        return reviewRepository.countByBusinessIdAndStatus(businessId, ReviewStatus.PENDING);
    }
}
