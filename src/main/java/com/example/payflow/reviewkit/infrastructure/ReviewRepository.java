package com.example.payflow.reviewkit.infrastructure;

import com.example.payflow.reviewkit.domain.Review;
import com.example.payflow.reviewkit.domain.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("reviewKitReviewRepository")
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByBusinessIdOrderByCreatedAtDesc(Long businessId);
    
    List<Review> findByBusinessIdAndStatusOrderByCreatedAtDesc(Long businessId, ReviewStatus status);
    
    long countByBusinessIdAndStatus(Long businessId, ReviewStatus status);
}
