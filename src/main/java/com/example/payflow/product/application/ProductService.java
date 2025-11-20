package com.example.payflow.product.application;

import com.example.payflow.product.application.dto.*;
import com.example.payflow.product.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    
    /**
     * Home Feed용 상품 리스트 조회 (최신순)
     */
    public Page<ProductResponse> getHomeFeed(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByStatusOrderByCreatedAtDesc(
                ProductStatus.AVAILABLE, pageable);
        return products.map(ProductResponse::from);
    }
    
    /**
     * 카테고리별 상품 조회
     */
    public Page<ProductResponse> getProductsByCategory(ProductCategory category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByCategoryAndStatusOrderByCreatedAtDesc(
                category, ProductStatus.AVAILABLE, pageable);
        return products.map(ProductResponse::from);
    }
    
    /**
     * 인기 상품 조회 (좋아요 많은 순)
     */
    public Page<ProductResponse> getPopularProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByStatusOrderByLikeCountDescCreatedAtDesc(
                ProductStatus.AVAILABLE, pageable);
        return products.map(ProductResponse::from);
    }
    
    /**
     * 키워드로 상품 검색
     */
    public Page<ProductResponse> searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.searchByKeyword(
                keyword, ProductStatus.AVAILABLE, pageable);
        return products.map(ProductResponse::from);
    }
    
    /**
     * 가격 범위로 상품 검색
     */
    public Page<ProductResponse> getProductsByPriceRange(
            BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByStatusAndPriceBetweenOrderByCreatedAtDesc(
                ProductStatus.AVAILABLE, minPrice, maxPrice, pageable);
        return products.map(ProductResponse::from);
    }
    
    /**
     * 지역별 상품 조회
     */
    public Page<ProductResponse> getProductsByLocation(String location, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByLocationAndStatusOrderByCreatedAtDesc(
                location, ProductStatus.AVAILABLE, pageable);
        return products.map(ProductResponse::from);
    }
    
    /**
     * 상품 상세 조회
     */
    @Transactional
    public ProductDetailResponse getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        
        // 조회수 증가
        product.incrementViewCount();
        
        return ProductDetailResponse.from(product);
    }
    
    /**
     * 판매자의 상품 목록 조회 (ID 기준)
     */
    public Page<ProductResponse> getSellerProducts(Long sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findBySellerIdAndStatusOrderByCreatedAtDesc(
                sellerId, ProductStatus.AVAILABLE, pageable);
        return products.map(ProductResponse::from);
    }
    
    /**
     * 판매자의 상품 목록 조회 (이름 기준)
     */
    public Page<ProductResponse> getProductsBySellerName(String sellerName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findBySellerNameAndStatusOrderByCreatedAtDesc(
                sellerName, ProductStatus.AVAILABLE, pageable);
        return products.map(ProductResponse::from);
    }
    
    /**
     * 상품 등록
     */
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = new Product(
                request.getTitle(),
                request.getDescription(),
                request.getPrice(),
                request.getCategory(),
                request.getProductCondition(),
                request.getSellerId(),
                request.getSellerName(),
                request.getLocation(),
                request.getImageUrls()
        );
        
        Product savedProduct = productRepository.save(product);
        log.info("상품 등록 완료: productId={}, title={}", savedProduct.getId(), savedProduct.getTitle());
        
        return ProductResponse.from(savedProduct);
    }
    
    /**
     * 상품 수정
     */
    @Transactional
    public ProductResponse updateProduct(Long productId, UpdateProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        
        product.updateProduct(
                request.getTitle(),
                request.getDescription(),
                request.getPrice(),
                request.getCategory(),
                request.getProductCondition(),
                request.getLocation(),
                request.getImageUrls()
        );
        
        log.info("상품 수정 완료: productId={}", productId);
        return ProductResponse.from(product);
    }
    
    /**
     * 상품 삭제
     */
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        
        product.delete();
        log.info("상품 삭제 완료: productId={}", productId);
    }
    
    /**
     * 상품 판매 완료 처리
     */
    @Transactional
    public void markAsSold(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        
        product.markAsSold();
        log.info("상품 판매 완료: productId={}", productId);
    }
    
    /**
     * 상품 좋아요
     */
    @Transactional
    public void likeProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        
        product.incrementLikeCount();
    }
    
    /**
     * 상품 좋아요 취소
     */
    @Transactional
    public void unlikeProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        
        product.decrementLikeCount();
    }
}
