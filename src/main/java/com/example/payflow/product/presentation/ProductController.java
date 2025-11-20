package com.example.payflow.product.presentation;

import com.example.payflow.product.application.ProductService;
import com.example.payflow.product.application.dto.*;
import com.example.payflow.product.domain.ProductCategory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    
    private final ProductService productService;
    
    /**
     * Home Feed - 최신 상품 리스트 조회
     * GET /api/products/feed?page=0&size=20
     */
    @GetMapping("/feed")
    public ResponseEntity<Page<ProductResponse>> getHomeFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Home Feed 조회 요청: page={}, size={}", page, size);
        Page<ProductResponse> products = productService.getHomeFeed(page, size);
        return ResponseEntity.ok(products);
    }
    
    /**
     * 인기 상품 조회 (좋아요 많은 순)
     * GET /api/products/popular?page=0&size=20
     */
    @GetMapping("/popular")
    public ResponseEntity<Page<ProductResponse>> getPopularProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("인기 상품 조회 요청: page={}, size={}", page, size);
        Page<ProductResponse> products = productService.getPopularProducts(page, size);
        return ResponseEntity.ok(products);
    }
    
    /**
     * 카테고리별 상품 조회
     * GET /api/products/category/ELECTRONICS?page=0&size=20
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<ProductResponse>> getProductsByCategory(
            @PathVariable ProductCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("카테고리별 상품 조회: category={}, page={}, size={}", category, page, size);
        Page<ProductResponse> products = productService.getProductsByCategory(category, page, size);
        return ResponseEntity.ok(products);
    }
    
    /**
     * 키워드로 상품 검색
     * GET /api/products/search?keyword=아이폰&page=0&size=20
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("상품 검색: keyword={}, page={}, size={}", keyword, page, size);
        Page<ProductResponse> products = productService.searchProducts(keyword, page, size);
        return ResponseEntity.ok(products);
    }
    
    /**
     * 가격 범위로 상품 검색
     * GET /api/products/price-range?minPrice=10000&maxPrice=50000&page=0&size=20
     */
    @GetMapping("/price-range")
    public ResponseEntity<Page<ProductResponse>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("가격 범위 검색: minPrice={}, maxPrice={}, page={}, size={}", 
                minPrice, maxPrice, page, size);
        Page<ProductResponse> products = productService.getProductsByPriceRange(
                minPrice, maxPrice, page, size);
        return ResponseEntity.ok(products);
    }
    
    /**
     * 지역별 상품 조회
     * GET /api/products/location?location=서울&page=0&size=20
     */
    @GetMapping("/location")
    public ResponseEntity<Page<ProductResponse>> getProductsByLocation(
            @RequestParam String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("지역별 상품 조회: location={}, page={}, size={}", location, page, size);
        Page<ProductResponse> products = productService.getProductsByLocation(location, page, size);
        return ResponseEntity.ok(products);
    }
    
    /**
     * 상품 상세 조회
     * GET /api/products/1
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(@PathVariable Long productId) {
        log.info("상품 상세 조회: productId={}", productId);
        ProductDetailResponse product = productService.getProductDetail(productId);
        return ResponseEntity.ok(product);
    }
    
    /**
     * 판매자의 상품 목록 조회
     * GET /api/products/seller/1?page=0&size=20
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<Page<ProductResponse>> getSellerProducts(
            @PathVariable Long sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("판매자 상품 조회: sellerId={}, page={}, size={}", sellerId, page, size);
        Page<ProductResponse> products = productService.getSellerProducts(sellerId, page, size);
        return ResponseEntity.ok(products);
    }
    
    /**
     * 내 상품 목록 조회 (현재 로그인한 사용자)
     * GET /api/products/my?page=0&size=20
     */
    @GetMapping("/my")
    public ResponseEntity<Page<ProductResponse>> getMyProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        
        log.info("내 상품 조회 요청: page={}, size={}", page, size);
        // TODO: 실제로는 SecurityContext에서 현재 사용자 ID를 가져와야 함
        // 현재는 개발 단계이므로 임시로 sellerId=1 사용
        Page<ProductResponse> products = productService.getSellerProducts(1L, page, size);
        return ResponseEntity.ok(products);
    }
    
    /**
     * 상품 등록
     * POST /api/products
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        
        log.info("상품 등록 요청: title={}, price={}", request.getTitle(), request.getPrice());
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }
    
    /**
     * 상품 수정
     * PUT /api/products/1
     */
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequest request) {
        
        log.info("상품 수정 요청: productId={}", productId);
        ProductResponse product = productService.updateProduct(productId, request);
        return ResponseEntity.ok(product);
    }
    
    /**
     * 상품 삭제
     * DELETE /api/products/1
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        log.info("상품 삭제 요청: productId={}", productId);
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 상품 판매 완료 처리
     * POST /api/products/1/sold
     */
    @PostMapping("/{productId}/sold")
    public ResponseEntity<Void> markAsSold(@PathVariable Long productId) {
        log.info("상품 판매 완료 처리: productId={}", productId);
        productService.markAsSold(productId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 상품 좋아요
     * POST /api/products/1/like
     */
    @PostMapping("/{productId}/like")
    public ResponseEntity<Void> likeProduct(@PathVariable Long productId) {
        log.info("상품 좋아요: productId={}", productId);
        productService.likeProduct(productId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 상품 좋아요 취소
     * DELETE /api/products/1/like
     */
    @DeleteMapping("/{productId}/like")
    public ResponseEntity<Void> unlikeProduct(@PathVariable Long productId) {
        log.info("상품 좋아요 취소: productId={}", productId);
        productService.unlikeProduct(productId);
        return ResponseEntity.ok().build();
    }
}
