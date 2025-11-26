package com.example.payflow.catalog.presentation.dto;

import com.example.payflow.catalog.domain.OrderCart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {
    
    private Long id;
    private String storeId;
    private String distributorId;
    private List<CartItemResponse> items;
    private Long totalAmount;
    private Integer totalQuantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static CartResponse from(OrderCart cart) {
        return CartResponse.builder()
                .id(cart.getId())
                .storeId(cart.getStoreId())
                .distributorId(cart.getDistributorId())
                .items(cart.getItems().stream()
                        .map(CartItemResponse::from)
                        .collect(Collectors.toList()))
                .totalAmount(cart.getTotalAmount())
                .totalQuantity(cart.getTotalQuantity())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
