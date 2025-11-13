package com.example.payflow.escrow.domain;

public class InvalidEscrowStateException extends RuntimeException {
    public InvalidEscrowStateException(String message) {
        super(message);
    }
}
