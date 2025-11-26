package com.example.payflow.catalog.application;

import com.example.payflow.catalog.domain.ProductCatalog;
import com.example.payflow.catalog.domain.ProductCatalogRepository;
import com.example.payflow.catalog.domain.ProductDeliveryInfo;
import com.example.payflow.catalog.domain.ProductDeliveryInfoRepository;
import com.example.payflow.catalog.presentation.dto.CreateProductRequest;
import com.example.payflow.catalog.presentation.dto.ProductDetailResponse;
import com.example.payflow.catalog.presentation.dto.UpdateProductRequest;
import com.example.payflow.distributor.domain.Distributor;
import com.example.payflow.distributor.domain.DistributorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCatalogService {
    
    private final ProductCatalogRepository catalogRepository;
    private final ProductDeliveryInfoRepository deliveryInfoRepository;
    private final DistributorRepository distributorRepository;
    
    /**
     * 상품 등록
     */
    @Transactional
    public ProductCatalog createProduct(String distributorId, CreateProductRequest request) {
        ProductCatalog product = ProductCatalog.builder()
                .distributorId(distributorId)
                .productName(request.getProductName())
                .category(request.getCategory())
                .description(request.getDescription())
                .unitPrice(request.getUnitPrice())
                .unit(request.getUnit())
                .stockQuantity(request.getStockQuantity())
                .origin(request.getOrigin())
                .brand(request.getBrand())
                .imageUrl(request.getImageUrl())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .minOrderQuantity(request.getMinOrderQuantity())
                .maxOrderQuantity(request.getMaxOrderQuantity())
                .certifications(request.getCertifications())
                .build();
        
        ProductCatalog saved = catalogRepository.save(product);
        log.info("✅ 상품 등록 완료: distributorId={}, productName={}, id={}", 
                distributorId, request.getProductName(), saved.getId());
        
        return saved;
    }
    
    /**
     * 상품 수정
     */
    @Transactional
    public ProductCatalog updateProduct(String distributorId, Long productId, UpdateProductRequest request) {
        ProductCatalog product = catalogRepository.findByIdAndDistributorId(productId, distributorId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));
        
        if (request.getProductName() != null) {
            product.setProductName(request.getProductName());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getUnitPrice() != null) {
            product.setUnitPrice(request.getUnitPrice());
        }
        if (request.getUnit() != null) {
            product.setUnit(request.getUnit());
        }
        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }
        if (request.getOrigin() != null) {
            product.setOrigin(request.getOrigin());
        }
        if (request.getBrand() != null) {
            product.setBrand(request.getBrand());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getIsAvailable() != null) {
            product.setIsAvailable(request.getIsAvailable());
        }
        if (request.getMinOrderQuantity() != null) {
            product.setMinOrderQuantity(request.getMinOrderQuantity());
        }
        if (request.getMaxOrderQuantity() != null) {
            product.setMaxOrderQuantity(request.getMaxOrderQuantity());
        }
        if (request.getCertifications() != null) {
            product.setCertifications(request.getCertifications());
        }
        
        ProductCatalog updated = catalogRepository.save(product);
        log.info("✅ 상품 수정 완료: productId={}, distributorId={}", productId, distributorId);
        
        return updated;
    }
    
    /**
     * 상품 삭제
     */
    @Transactional
    public void deleteProduct(String distributorId, Long productId) {
        ProductCatalog product = catalogRepository.findByIdAndDistributorId(productId, distributorId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));
        
        catalogRepository.delete(product);
        log.info("✅ 상품 삭제 완료: productId={}, distributorId={}", productId, distributorId);
    }
    
    /**
     * 유통업체 카탈로그 전체 조회
     */
    @Transactional(readOnly = true)
    public List<ProductCatalog> getDistributorCatalog(String distributorId) {
        return catalogRepository.findByDistributorId(distributorId);
    }
    
    /**
     * 판매 가능한 상품만 조회
     */
    @Transactional(readOnly = true)
    public List<ProductCatalog> getAvailableProducts(String distributorId) {
        return catalogRepository.findByDistributorIdAndIsAvailable(distributorId, true);
    }
    
    /**
     * 카테고리별 상품 조회
     */
    @Transactional(readOnly = true)
    public List<ProductCatalog> getProductsByCategory(String distributorId, String category) {
        return catalogRepository.findByDistributorIdAndCategory(distributorId, category);
    }
    
    /**
     * 상품명 검색
     */
    @Transactional(readOnly = true)
    public List<ProductCatalog> searchProducts(String distributorId, String keyword) {
        return catalogRepository.searchByProductName(distributorId, keyword);
    }
    
    /**
     * 가격 범위로 검색
     */
    @Transactional(readOnly = true)
    public List<ProductCatalog> searchByPriceRange(String distributorId, Long minPrice, Long maxPrice) {
        return catalogRepository.findByPriceRange(distributorId, minPrice, maxPrice);
    }
    
    /**
     * 재고 있는 상품만 조회
     */
    @Transactional(readOnly = true)
    public List<ProductCatalog> getProductsWithStock(String distributorId) {
        return catalogRepository.findAvailableWithStock(distributorId);
    }
    
    /**
     * 상품 상세 조회
     */
    @Transactional(readOnly = true)
    public ProductCatalog getProduct(Long productId) {
        return catalogRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));
    }
    
    /**
     * 상품 상세 정보 조회 (가격, 재고, 배송 정보 포함)
     */
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetail(Long productId) {
        ProductCatalog product = catalogRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));
        
        ProductDeliveryInfo deliveryInfo = deliveryInfoRepository.findByProductId(productId)
                .orElse(null);
        
        String distributorName = distributorRepository.findByDistributorId(product.getDistributorId())
                .map(Distributor::getDistributorName)
                .orElse(product.getDistributorId());
        
        return ProductDetailResponse.from(product, deliveryInfo, distributorName);
    }
    
    /**
     * 배송 정보 등록/수정
     */
    @Transactional
    public ProductDeliveryInfo saveDeliveryInfo(String distributorId, Long productId, ProductDeliveryInfo deliveryInfo) {
        // 상품이 해당 유통업체 소유인지 확인
        ProductCatalog product = catalogRepository.findByIdAndDistributorId(productId, distributorId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));
        
        // 기존 배송 정보 조회
        ProductDeliveryInfo existing = deliveryInfoRepository.findByProductId(productId)
                .orElse(null);
        
        if (existing != null) {
            // 업데이트
            existing.setDeliveryType(deliveryInfo.getDeliveryType());
            existing.setDeliveryFee(deliveryInfo.getDeliveryFee());
            existing.setFreeDeliveryThreshold(deliveryInfo.getFreeDeliveryThreshold());
            existing.setDeliveryRegions(deliveryInfo.getDeliveryRegions());
            existing.setDeliveryDays(deliveryInfo.getDeliveryDays());
            existing.setDeliveryTimeSlots(deliveryInfo.getDeliveryTimeSlots());
            existing.setEstimatedDeliveryDays(deliveryInfo.getEstimatedDeliveryDays());
            existing.setPackagingType(deliveryInfo.getPackagingType());
            existing.setIsFragile(deliveryInfo.getIsFragile());
            existing.setRequiresRefrigeration(deliveryInfo.getRequiresRefrigeration());
            existing.setSpecialInstructions(deliveryInfo.getSpecialInstructions());
            return deliveryInfoRepository.save(existing);
        } else {
            // 신규 등록
            deliveryInfo.setProductId(productId);
            return deliveryInfoRepository.save(deliveryInfo);
        }
    }
    
    /**
     * 재고 업데이트
     */
    @Transactional
    public ProductCatalog updateStock(String distributorId, Long productId, Integer quantity) {
        ProductCatalog product = catalogRepository.findByIdAndDistributorId(productId, distributorId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));
        
        product.setStockQuantity(quantity);
        ProductCatalog updated = catalogRepository.save(product);
        
        log.info("✅ 재고 업데이트: productId={}, quantity={}", productId, quantity);
        
        return updated;
    }
    
    /**
     * 판매 상태 변경
     */
    @Transactional
    public ProductCatalog toggleAvailability(String distributorId, Long productId) {
        ProductCatalog product = catalogRepository.findByIdAndDistributorId(productId, distributorId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));
        
        product.setIsAvailable(!product.getIsAvailable());
        ProductCatalog updated = catalogRepository.save(product);
        
        log.info("✅ 판매 상태 변경: productId={}, isAvailable={}", productId, updated.getIsAvailable());
        
        return updated;
    }
}
