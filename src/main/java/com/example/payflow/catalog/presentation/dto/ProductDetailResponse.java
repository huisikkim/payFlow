package com.example.payflow.catalog.presentation.dto;

import com.example.payflow.catalog.domain.ProductCatalog;
import com.example.payflow.catalog.domain.ProductDeliveryInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailResponse {
    
    // 기본 정보
    private Long id;
    private String distributorId;
    private String distributorName;
    private String productName;
    private String category;
    private String description;
    
    // 가격 정보
    private Long unitPrice;
    private String unit;
    private String priceInfo;  // "1포당 45,000원"
    private Boolean hasDiscount;
    private Integer discountRate;
    private Long discountedPrice;
    
    // 재고 정보
    private Integer stockQuantity;
    private String stockStatus;  // "재고 충분", "재고 부족", "품절"
    private Boolean isAvailable;
    private Integer minOrderQuantity;
    private Integer maxOrderQuantity;
    private String orderLimitInfo;  // "최소 1개 ~ 최대 50개"
    
    // 상품 상세
    private String origin;
    private String brand;
    private String imageUrl;
    private String certifications;
    
    // 배송 정보
    private DeliveryInfo deliveryInfo;
    
    // 메타 정보
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeliveryInfo {
        private String deliveryType;  // 당일배송, 익일배송, 일반배송
        private Integer deliveryFee;
        private Integer freeDeliveryThreshold;
        private String deliveryFeeInfo;  // "배송비 3,000원 (50,000원 이상 무료)"
        private String deliveryRegions;
        private String deliveryDays;
        private String deliveryTimeSlots;
        private Integer estimatedDeliveryDays;
        private String estimatedDeliveryInfo;  // "주문 후 1-2일 내 배송"
        private String packagingType;
        private Boolean isFragile;
        private Boolean requiresRefrigeration;
        private String specialInstructions;
    }
    
    public static ProductDetailResponse from(ProductCatalog product, ProductDeliveryInfo deliveryInfo, String distributorName) {
        ProductDetailResponseBuilder builder = ProductDetailResponse.builder()
                .id(product.getId())
                .distributorId(product.getDistributorId())
                .distributorName(distributorName)
                .productName(product.getProductName())
                .category(product.getCategory())
                .description(product.getDescription())
                .unitPrice(product.getUnitPrice())
                .unit(product.getUnit())
                .priceInfo(String.format("1%s당 %,d원", product.getUnit(), product.getUnitPrice()))
                .hasDiscount(false)
                .stockQuantity(product.getStockQuantity())
                .stockStatus(getStockStatus(product))
                .isAvailable(product.getIsAvailable())
                .minOrderQuantity(product.getMinOrderQuantity())
                .maxOrderQuantity(product.getMaxOrderQuantity())
                .orderLimitInfo(getOrderLimitInfo(product))
                .origin(product.getOrigin())
                .brand(product.getBrand())
                .imageUrl(product.getImageUrl())
                .certifications(product.getCertifications())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt());
        
        if (deliveryInfo != null) {
            builder.deliveryInfo(DeliveryInfo.builder()
                    .deliveryType(deliveryInfo.getDeliveryType())
                    .deliveryFee(deliveryInfo.getDeliveryFee())
                    .freeDeliveryThreshold(deliveryInfo.getFreeDeliveryThreshold())
                    .deliveryFeeInfo(getDeliveryFeeInfo(deliveryInfo))
                    .deliveryRegions(deliveryInfo.getDeliveryRegions())
                    .deliveryDays(deliveryInfo.getDeliveryDays())
                    .deliveryTimeSlots(deliveryInfo.getDeliveryTimeSlots())
                    .estimatedDeliveryDays(deliveryInfo.getEstimatedDeliveryDays())
                    .estimatedDeliveryInfo(getEstimatedDeliveryInfo(deliveryInfo))
                    .packagingType(deliveryInfo.getPackagingType())
                    .isFragile(deliveryInfo.getIsFragile())
                    .requiresRefrigeration(deliveryInfo.getRequiresRefrigeration())
                    .specialInstructions(deliveryInfo.getSpecialInstructions())
                    .build());
        }
        
        return builder.build();
    }
    
    private static String getStockStatus(ProductCatalog product) {
        if (!product.getIsAvailable()) {
            return "품절";
        }
        if (product.getStockQuantity() == null) {
            return "재고 정보 없음";
        }
        if (product.getStockQuantity() == 0) {
            return "품절";
        }
        if (product.getStockQuantity() < 10) {
            return "재고 부족";
        }
        return "재고 충분";
    }
    
    private static String getOrderLimitInfo(ProductCatalog product) {
        if (product.getMinOrderQuantity() != null && product.getMaxOrderQuantity() != null) {
            return String.format("최소 %d%s ~ 최대 %d%s", 
                    product.getMinOrderQuantity(), product.getUnit(),
                    product.getMaxOrderQuantity(), product.getUnit());
        }
        if (product.getMinOrderQuantity() != null) {
            return String.format("최소 %d%s", product.getMinOrderQuantity(), product.getUnit());
        }
        if (product.getMaxOrderQuantity() != null) {
            return String.format("최대 %d%s", product.getMaxOrderQuantity(), product.getUnit());
        }
        return "주문 수량 제한 없음";
    }
    
    private static String getDeliveryFeeInfo(ProductDeliveryInfo info) {
        if (info.getDeliveryFee() == null) {
            return "배송비 정보 없음";
        }
        if (info.getDeliveryFee() == 0) {
            return "무료 배송";
        }
        if (info.getFreeDeliveryThreshold() != null) {
            return String.format("배송비 %,d원 (%,d원 이상 무료)", 
                    info.getDeliveryFee(), info.getFreeDeliveryThreshold());
        }
        return String.format("배송비 %,d원", info.getDeliveryFee());
    }
    
    private static String getEstimatedDeliveryInfo(ProductDeliveryInfo info) {
        if (info.getEstimatedDeliveryDays() == null) {
            return "배송 일정 미정";
        }
        if (info.getEstimatedDeliveryDays() == 0) {
            return "당일 배송";
        }
        if (info.getEstimatedDeliveryDays() == 1) {
            return "익일 배송";
        }
        return String.format("주문 후 %d일 내 배송", info.getEstimatedDeliveryDays());
    }
}
