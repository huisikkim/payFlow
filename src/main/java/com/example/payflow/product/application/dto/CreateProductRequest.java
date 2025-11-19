package com.example.payflow.product.application.dto;

import com.example.payflow.product.domain.ProductCategory;
import com.example.payflow.product.domain.ProductCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
    
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    
    private String description;
    
    @NotNull(message = "가격은 필수입니다.")
    @Positive(message = "가격은 0보다 커야 합니다.")
    private BigDecimal price;
    
    @NotNull(message = "카테고리는 필수입니다.")
    private ProductCategory category;
    
    @NotNull(message = "상품 상태는 필수입니다.")
    private ProductCondition productCondition;
    
    @NotNull(message = "판매자 ID는 필수입니다.")
    private Long sellerId;
    
    @NotBlank(message = "판매자 이름은 필수입니다.")
    private String sellerName;
    
    private String location;
    
    private List<String> imageUrls;
}
