package com.example.payflow.settlement.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CompleteSettlementRequest {
    
    @NotNull(message = "지불 금액은 필수입니다.")
    @Min(value = 0, message = "지불 금액은 0 이상이어야 합니다.")
    private Long paidAmount;
    
    public CompleteSettlementRequest(Long paidAmount) {
        this.paidAmount = paidAmount;
    }
}
