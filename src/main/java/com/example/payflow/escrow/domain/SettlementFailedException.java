package com.example.payflow.escrow.domain;

public class SettlementFailedException extends RuntimeException {
    public SettlementFailedException(String message) {
        super(message);
    }
    
    public SettlementFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
