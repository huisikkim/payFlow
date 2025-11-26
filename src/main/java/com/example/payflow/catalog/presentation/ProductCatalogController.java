package com.example.payflow.catalog.presentation;

import com.example.payflow.catalog.application.ProductCatalogService;
import com.example.payflow.catalog.domain.ProductCatalog;
import com.example.payflow.catalog.domain.ProductDeliveryInfo;
import com.example.payflow.catalog.presentation.dto.CreateProductRequest;
import com.example.payflow.catalog.presentation.dto.ProductDetailResponse;
import com.example.payflow.catalog.presentation.dto.UpdateProductRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
@Slf4j
public class ProductCatalogController {
    
    private final ProductCatalogService catalogService;
    
    /**
     * 상품 등록 (유통업체)
     * POST /api/catalog/products
     */
    @PostMapping("/products")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public ResponseEntity<ProductCatalog> createProduct(@Valid @RequestBody CreateProductRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String distributorId = authentication.getName();
        
        ProductCatalog product = catalogService.createProduct(distributorId, request);
        return ResponseEntity.ok(product);
    }
    
    /**
     * 상품 수정 (유통업체)
     * PUT /api/catalog/products/{productId}
     */
    @PutMapping("/products/{productId}")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public ResponseEntity<ProductCatalog> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String distributorId = authentication.getName();
        
        ProductCatalog product = catalogService.updateProduct(distributorId, productId, request);
        return ResponseEntity.ok(product);
    }
    
    /**
     * 상품 삭제 (유통업체)
     * DELETE /api/catalog/products/{productId}
     */
    @DeleteMapping("/products/{productId}")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public ResponseEntity<String> deleteProduct(@PathVariable Long productId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String distributorId = authentication.getName();
        
        catalogService.deleteProduct(distributorId, productId);
        return ResponseEntity.ok("상품이 삭제되었습니다.");
    }
    
    /**
     * 내 카탈로그 조회 (유통업체)
     * GET /api/catalog/my-products
     */
    @GetMapping("/my-products")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public ResponseEntity<List<ProductCatalog>> getMyProducts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String distributorId = authentication.getName();
        
        List<ProductCatalog> products = catalogService.getDistributorCatalog(distributorId);
        return ResponseEntity.ok(products);
    }
    
    /**
     * 유통업체 카탈로그 조회 (매장)
     * GET /api/catalog/distributor/{distributorId}
     */
    @GetMapping("/distributor/{distributorId}")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<List<ProductCatalog>> getDistributorCatalog(@PathVariable String distributorId) {
        List<ProductCatalog> products = catalogService.getAvailableProducts(distributorId);
        return ResponseEntity.ok(products);
    }
    
    /**
     * 카테고리별 상품 조회 (매장)
     * GET /api/catalog/distributor/{distributorId}/category/{category}
     */
    @GetMapping("/distributor/{distributorId}/category/{category}")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<List<ProductCatalog>> getProductsByCategory(
            @PathVariable String distributorId,
            @PathVariable String category) {
        List<ProductCatalog> products = catalogService.getProductsByCategory(distributorId, category);
        return ResponseEntity.ok(products);
    }
    
    /**
     * 상품 검색 (매장)
     * GET /api/catalog/distributor/{distributorId}/search?keyword=쌀
     */
    @GetMapping("/distributor/{distributorId}/search")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<List<ProductCatalog>> searchProducts(
            @PathVariable String distributorId,
            @RequestParam String keyword) {
        List<ProductCatalog> products = catalogService.searchProducts(distributorId, keyword);
        return ResponseEntity.ok(products);
    }
    
    /**
     * 가격 범위로 검색 (매장)
     * GET /api/catalog/distributor/{distributorId}/price-range?minPrice=1000&maxPrice=10000
     */
    @GetMapping("/distributor/{distributorId}/price-range")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<List<ProductCatalog>> searchByPriceRange(
            @PathVariable String distributorId,
            @RequestParam Long minPrice,
            @RequestParam Long maxPrice) {
        List<ProductCatalog> products = catalogService.searchByPriceRange(distributorId, minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }
    
    /**
     * 재고 있는 상품만 조회 (매장)
     * GET /api/catalog/distributor/{distributorId}/in-stock
     */
    @GetMapping("/distributor/{distributorId}/in-stock")
    @PreAuthorize("hasRole('STORE_OWNER')")
    public ResponseEntity<List<ProductCatalog>> getProductsWithStock(@PathVariable String distributorId) {
        List<ProductCatalog> products = catalogService.getProductsWithStock(distributorId);
        return ResponseEntity.ok(products);
    }
    
    /**
     * 상품 상세 조회
     * GET /api/catalog/products/{productId}
     */
    @GetMapping("/products/{productId}")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'DISTRIBUTOR')")
    public ResponseEntity<ProductCatalog> getProduct(@PathVariable Long productId) {
        ProductCatalog product = catalogService.getProduct(productId);
        return ResponseEntity.ok(product);
    }
    
    /**
     * 재고 업데이트 (유통업체)
     * PUT /api/catalog/products/{productId}/stock
     */
    @PutMapping("/products/{productId}/stock")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public ResponseEntity<ProductCatalog> updateStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String distributorId = authentication.getName();
        
        ProductCatalog product = catalogService.updateStock(distributorId, productId, quantity);
        return ResponseEntity.ok(product);
    }
    
    /**
     * 판매 상태 변경 (유통업체)
     * PUT /api/catalog/products/{productId}/toggle-availability
     */
    @PutMapping("/products/{productId}/toggle-availability")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public ResponseEntity<ProductCatalog> toggleAvailability(@PathVariable Long productId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String distributorId = authentication.getName();
        
        ProductCatalog product = catalogService.toggleAvailability(distributorId, productId);
        return ResponseEntity.ok(product);
    }
    
    /**
     * 상품 상세 정보 조회 (가격, 재고, 배송 정보 포함)
     * GET /api/catalog/products/{productId}/detail
     */
    @GetMapping("/products/{productId}/detail")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'DISTRIBUTOR')")
    public ResponseEntity<ProductDetailResponse> getProductDetail(@PathVariable Long productId) {
        ProductDetailResponse detail = catalogService.getProductDetail(productId);
        return ResponseEntity.ok(detail);
    }
    
    /**
     * 배송 정보 등록/수정 (유통업체)
     * POST /api/catalog/products/{productId}/delivery-info
     */
    @PostMapping("/products/{productId}/delivery-info")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public ResponseEntity<ProductDeliveryInfo> saveDeliveryInfo(
            @PathVariable Long productId,
            @Valid @RequestBody ProductDeliveryInfo deliveryInfo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String distributorId = authentication.getName();
        
        ProductDeliveryInfo saved = catalogService.saveDeliveryInfo(distributorId, productId, deliveryInfo);
        return ResponseEntity.ok(saved);
    }
}
