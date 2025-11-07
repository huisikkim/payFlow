package com.example.payflow.stage.presentation.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateStageRequest {

    @NotBlank(message = "스테이지 이름은 필수입니다.")
    private String name;

    @NotNull(message = "참여 인원은 필수입니다.")
    @Min(value = 2, message = "참여 인원은 최소 2명 이상이어야 합니다.")
    @Max(value = 50, message = "참여 인원은 최대 50명까지 가능합니다.")
    private Integer totalParticipants;

    @NotNull(message = "월 납입액은 필수입니다.")
    @DecimalMin(value = "1000", message = "월 납입액은 최소 1,000원 이상이어야 합니다.")
    private BigDecimal monthlyPayment;

    @NotNull(message = "이율은 필수입니다.")
    @DecimalMin(value = "0.0", message = "이율은 0 이상이어야 합니다.")
    @DecimalMax(value = "1.0", message = "이율은 100% 이하여야 합니다.")
    private BigDecimal interestRate;

    @NotNull(message = "결제일은 필수입니다.")
    @Min(value = 1, message = "결제일은 1일 이상이어야 합니다.")
    @Max(value = 28, message = "결제일은 28일 이하여야 합니다.")
    private Integer paymentDay;
}
