package com.example.payflow.saga.presentation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestOrderRequest {
    private String productId;
    private String productName;
    private Long amount;
    private String customerEmail;
    private String customerName;
}
