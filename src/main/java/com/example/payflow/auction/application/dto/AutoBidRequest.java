package com.example.payflow.auction.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoBidRequest {
    
    @NotNull(message = "최대 입찰 금액은 필수입니다.")
    @DecimalMin(value = "0.0", inclusive = false, message = "최대 입찰 금액은 0보다 커야 합니다.")
    private BigDecimal maxAmount;
}
