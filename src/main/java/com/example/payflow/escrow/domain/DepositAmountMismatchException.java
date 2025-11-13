package com.example.payflow.escrow.domain;

import java.math.BigDecimal;

public class DepositAmountMismatchException extends RuntimeException {
    public DepositAmountMismatchException(BigDecimal expected, BigDecimal actual) {
        super(String.format("입금 금액 불일치: 예상=%s, 실제=%s", expected, actual));
    }
}
