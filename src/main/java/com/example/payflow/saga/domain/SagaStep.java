package com.example.payflow.saga.domain;

public enum SagaStep {
    ORDER_CREATED,
    PAYMENT_PROCESSED,
    INVENTORY_RESERVED,
    COMPLETED
}
