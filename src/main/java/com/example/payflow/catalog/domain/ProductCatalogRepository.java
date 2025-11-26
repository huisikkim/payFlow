package com.example.payflow.catalog.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCatalogRepository extends JpaRepository<ProductCatalog, Long> {
    
    /**
     * 유통업체별 카탈로그 조회
     */
    List<ProductCatalog> findByDistributorId(String distributorId);
    
    /**
     * 유통업체별 판매 가능한 상품 조회
     */
    List<ProductCatalog> findByDistributorIdAndIsAvailable(String distributorId, Boolean isAvailable);
    
    /**
     * 카테고리별 상품 조회
     */
    List<ProductCatalog> findByDistributorIdAndCategory(String distributorId, String category);
    
    /**
     * 상품명 검색
     */
    @Query("SELECT p FROM ProductCatalog p WHERE p.distributorId = :distributorId AND p.productName LIKE %:keyword%")
    List<ProductCatalog> searchByProductName(String distributorId, String keyword);
    
    /**
     * 가격 범위로 검색
     */
    @Query("SELECT p FROM ProductCatalog p WHERE p.distributorId = :distributorId AND p.unitPrice BETWEEN :minPrice AND :maxPrice")
    List<ProductCatalog> findByPriceRange(String distributorId, Long minPrice, Long maxPrice);
    
    /**
     * 재고 있는 상품만 조회
     */
    @Query("SELECT p FROM ProductCatalog p WHERE p.distributorId = :distributorId AND p.isAvailable = true AND p.stockQuantity > 0")
    List<ProductCatalog> findAvailableWithStock(String distributorId);
    
    /**
     * 특정 상품 조회
     */
    Optional<ProductCatalog> findByIdAndDistributorId(Long id, String distributorId);
}
