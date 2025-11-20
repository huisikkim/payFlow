package com.example.payflow.auction.application.dto;

import com.example.payflow.auction.domain.AutoBid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoBidResponse {
    
    private Long id;
    private Long auctionId;
    private String bidderId;
    private BigDecimal maxAmount;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static AutoBidResponse from(AutoBid autoBid) {
        return AutoBidResponse.builder()
                .id(autoBid.getId())
                .auctionId(autoBid.getAuctionId())
                .bidderId(autoBid.getBidderId())
                .maxAmount(autoBid.getMaxAmount())
                .isActive(autoBid.getIsActive())
                .createdAt(autoBid.getCreatedAt())
                .updatedAt(autoBid.getUpdatedAt())
                .build();
    }
}
