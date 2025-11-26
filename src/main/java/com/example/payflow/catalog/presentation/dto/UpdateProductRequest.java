package com.example.payflow.catalog.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {
    
    private String productName;
    private String category;
    private String description;
    private Long unitPrice;
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
