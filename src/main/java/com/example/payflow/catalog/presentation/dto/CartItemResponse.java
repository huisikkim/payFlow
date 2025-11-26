package com.example.payflow.catalog.presentation.dto;

import com.example.payflow.catalog.domain.OrderCartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    
    private Long id;
    private Long productId;
    private String productName;
    private Long unitPrice;
    private String unit;
    private Integer quantity;
    private Long subtotal;
    private String imageUrl;
    
    public static CartItemResponse from(OrderCartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .unitPrice(item.getUnitPrice())
                .unit(item.getUnit())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .imageUrl(item.getImageUrl())
                .build();
    }
}
