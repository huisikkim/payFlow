package com.example.payflow.stage.presentation.dto;

import com.example.payflow.stage.domain.StagePayment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StagePaymentResponse {
    private Long id;
    private Long stageId;
    private String stageName;
    private String username;
    private Integer monthNumber;
    private BigDecimal amount;
    private Boolean isPaid;
    private LocalDateTime dueDate;
    private LocalDateTime paidAt;
    private String paymentKey;

    public static StagePaymentResponse from(StagePayment payment) {
        return new StagePaymentResponse(
                payment.getId(),
                payment.getStage().getId(),
                payment.getStage().getName(),
                payment.getUsername(),
                payment.getMonthNumber(),
                payment.getAmount(),
                payment.getIsPaid(),
                payment.getDueDate(),
                payment.getPaidAt(),
                payment.getPaymentKey()
        );
    }
}
