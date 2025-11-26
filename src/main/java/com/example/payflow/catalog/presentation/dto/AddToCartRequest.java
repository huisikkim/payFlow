package com.example.payflow.catalog.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {
    
    @NotNull(message = "상품 ID는 필수입니다")
    private Long productId;
    
    @NotNull(message = "수량은 필수입니다")
    @Positive(message = "수량은 0보다 커야 합니다")
    private Integer quantity;
}
