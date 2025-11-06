package com.example.payflow.order.presentation.dto;

import com.example.payflow.order.domain.Order;
import com.example.payflow.order.domain.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrderResponse {
    private String orderId;
    private String orderName;
    private Long amount;
    private String customerEmail;
    private String customerName;
    private OrderStatus status;
    private LocalDateTime createdAt;
    
    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
            .orderId(order.getOrderId())
            .orderName(order.getOrderName())
            .amount(order.getAmount())
            .customerEmail(order.getCustomerEmail())
            .customerName(order.getCustomerName())
            .status(order.getStatus())
            .createdAt(order.getCreatedAt())
            .build();
    }
}
