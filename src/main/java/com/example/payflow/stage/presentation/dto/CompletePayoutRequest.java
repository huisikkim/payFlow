package com.example.payflow.stage.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompletePayoutRequest {

    @NotBlank(message = "거래 ID는 필수입니다.")
    private String transactionId;
}
