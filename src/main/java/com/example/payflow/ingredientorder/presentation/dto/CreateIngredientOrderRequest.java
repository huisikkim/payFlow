package com.example.payflow.ingredientorder.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CreateIngredientOrderRequest {
    
    @NotBlank(message = "매장 ID는 필수입니다.")
    private String storeId;
    
    @NotBlank(message = "유통사 ID는 필수입니다.")
    private String distributorId;
    
    @NotEmpty(message = "발주 품목은 최소 1개 이상이어야 합니다.")
    @Valid
    private List<IngredientOrderItemDto> items;
    
    public CreateIngredientOrderRequest(String storeId, String distributorId, List<IngredientOrderItemDto> items) {
        this.storeId = storeId;
        this.distributorId = distributorId;
        this.items = items;
    }
}
