package com.example.payflow.catalog.presentation.dto;

import com.example.payflow.catalog.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String storeId;
    private String distributorId;
    private String orderNumber;
    private List<OrderItemResponse> items;
    private Long totalAmount;
    private Integer totalQuantity;
    private OrderStatus status;
    private String statusDescription;
    private String deliveryAddress;
    private String deliveryPhone;
    private String deliveryRequest;
    private LocalDateTime orderedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
}
