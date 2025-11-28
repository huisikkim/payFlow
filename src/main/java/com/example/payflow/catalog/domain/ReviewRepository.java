package com.example.payflow.catalog.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // 주문별 리뷰 조회
    Optional<Review> findByOrderIdAndReviewType(Long orderId, ReviewType reviewType);
    
    // 리뷰 대상자별 받은 리뷰 조회
    List<Review> findByRevieweeIdOrderByCreatedAtDesc(String revieweeId);
    
    // 리뷰 작성자별 작성한 리뷰 조회
    List<Review> findByReviewerIdOrderByCreatedAtDesc(String reviewerId);
    
    // 리뷰 타입별 조회
    List<Review> findByReviewTypeOrderByCreatedAtDesc(ReviewType reviewType);
    
    // 평균 평점 계산 (리뷰 대상자별)
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.revieweeId = :revieweeId")
    Double calculateAverageRating(@Param("revieweeId") String revieweeId);
    
    // 평균 평점 계산 (리뷰 대상자별 + 타입별)
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.revieweeId = :revieweeId AND r.reviewType = :reviewType")
    Double calculateAverageRatingByType(@Param("revieweeId") String revieweeId, @Param("reviewType") ReviewType reviewType);
    
    // 리뷰 개수 (리뷰 대상자별)
    Long countByRevieweeId(String revieweeId);
    
    // 리뷰 개수 (리뷰 대상자별 + 타입별)
    Long countByRevieweeIdAndReviewType(String revieweeId, ReviewType reviewType);
    
    // 주문에 대한 리뷰 존재 여부 확인
    boolean existsByOrderIdAndReviewType(Long orderId, ReviewType reviewType);
}
