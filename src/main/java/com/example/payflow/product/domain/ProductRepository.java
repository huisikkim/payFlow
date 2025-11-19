package com.example.payflow.product.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Home Feed용 - 최신순 정렬
    Page<Product> findByStatusOrderByCreatedAtDesc(ProductStatus status, Pageable pageable);
    
    // 카테고리별 조회
    Page<Product> findByCategoryAndStatusOrderByCreatedAtDesc(
            ProductCategory category, ProductStatus status, Pageable pageable);
    
    // 판매자별 상품 조회
    Page<Product> findBySellerIdAndStatusOrderByCreatedAtDesc(
            Long sellerId, ProductStatus status, Pageable pageable);
    
    // 가격 범위로 검색
    Page<Product> findByStatusAndPriceBetweenOrderByCreatedAtDesc(
            ProductStatus status, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    // 제목 또는 설명으로 검색
    @Query("SELECT p FROM Product p WHERE p.status = :status AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.createdAt DESC")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, 
                                   @Param("status") ProductStatus status, 
                                   Pageable pageable);
    
    // 인기 상품 (좋아요 많은 순)
    Page<Product> findByStatusOrderByLikeCountDescCreatedAtDesc(
            ProductStatus status, Pageable pageable);
    
    // 지역별 조회
    Page<Product> findByLocationAndStatusOrderByCreatedAtDesc(
            String location, ProductStatus status, Pageable pageable);
    
    // 판매자의 전체 상품 수
    long countBySellerId(Long sellerId);
    
    // 카테고리별 상품 수
    long countByCategoryAndStatus(ProductCategory category, ProductStatus status);
}
