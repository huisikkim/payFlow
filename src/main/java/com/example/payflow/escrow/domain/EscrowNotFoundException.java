package com.example.payflow.escrow.domain;

public class EscrowNotFoundException extends RuntimeException {
    public EscrowNotFoundException(String transactionId) {
        super("에스크로 거래를 찾을 수 없습니다: " + transactionId);
    }
}
