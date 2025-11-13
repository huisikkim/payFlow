package com.example.payflow.escrow.application.dto;

import com.example.payflow.escrow.domain.Deposit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DepositResponse {
    private Long id;
    private String transactionId;
    private BigDecimal amount;
    private String depositMethod;
    private String depositReference;
    private LocalDateTime depositedAt;
    private LocalDateTime confirmedAt;
    private boolean confirmed;
    
    public static DepositResponse from(Deposit deposit) {
        return new DepositResponse(
            deposit.getId(),
            deposit.getTransactionId(),
            deposit.getAmount(),
            deposit.getDepositMethod(),
            deposit.getDepositReference(),
            deposit.getDepositedAt(),
            deposit.getConfirmedAt(),
            deposit.isConfirmed()
        );
    }
}
