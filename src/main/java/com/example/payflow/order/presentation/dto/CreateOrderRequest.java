package com.example.payflow.order.presentation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateOrderRequest {
    private String orderName;
    private Long amount;
    private String customerEmail;
    private String customerName;
}
