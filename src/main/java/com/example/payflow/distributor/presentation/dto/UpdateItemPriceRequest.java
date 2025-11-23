package com.example.payflow.distributor.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateItemPriceRequest {
    
    @NotNull(message = "새 단가는 필수입니다.")
    @Min(value = 0, message = "단가는 0 이상이어야 합니다.")
    private Long newPrice;
    
    public UpdateItemPriceRequest(Long newPrice) {
        this.newPrice = newPrice;
    }
}
