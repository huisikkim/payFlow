package com.example.payflow.catalog.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
    
    @NotBlank(message = "상품명은 필수입니다")
    private String productName;
    
    @NotBlank(message = "카테고리는 필수입니다")
    private String category;
    
    private String description;
    
    @NotNull(message = "단가는 필수입니다")
    @Positive(message = "단가는 0보다 커야 합니다")
    private Long unitPrice;
    
    @NotBlank(message = "단위는 필수입니다")
    private String unit;
    
    private Integer stockQuantity;
    
    private String origin;
    
    private String brand;
    
    private String imageUrl;
    
    private Boolean isAvailable;
    
    private Integer minOrderQuantity;
    
    private Integer maxOrderQuantity;
    
    private String certifications;
}
