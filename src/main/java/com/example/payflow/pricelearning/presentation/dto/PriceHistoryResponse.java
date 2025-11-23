package com.example.payflow.pricelearning.presentation.dto;

import com.example.payflow.pricelearning.domain.ItemPriceHistory;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PriceHistoryResponse {
    private Long id;
    private String itemName;
    private Long unitPrice;
    private String unit;
    private String orderId;
    private String distributorId;
    private String storeId;
    private LocalDateTime recordedAt;
    
    public static PriceHistoryResponse from(ItemPriceHistory history) {
        return new PriceHistoryResponse(
            history.getId(),
            history.getItemName(),
            history.getUnitPrice(),
            history.getUnit(),
            history.getOrderId(),
            history.getDistributorId(),
            history.getStoreId(),
            history.getRecordedAt()
        );
    }
}
