package com.example.payflow.distributor.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RejectOrderRequest {
    
    @NotBlank(message = "거절 사유는 필수입니다.")
    private String reason;
    
    public RejectOrderRequest(String reason) {
        this.reason = reason;
    }
}
