package com.example.payflow.product.application.dto;

import com.example.payflow.product.domain.Product;
import com.example.payflow.product.domain.ProductCategory;
import com.example.payflow.product.domain.ProductCondition;
import com.example.payflow.product.domain.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private ProductCategory category;
    private ProductCondition productCondition;
    private ProductStatus status;
    private Long sellerId;
    private String sellerName;
    private String location;
    private List<String> imageUrls;
    private Integer viewCount;
    private Integer likeCount;
    private Integer chatCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .productCondition(product.getProductCondition())
                .status(product.getStatus())
                .sellerId(product.getSellerId())
                .sellerName(product.getSellerName())
                .location(product.getLocation())
                .imageUrls(product.getImageUrls())
                .viewCount(product.getViewCount())
                .likeCount(product.getLikeCount())
                .chatCount(product.getChatCount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
