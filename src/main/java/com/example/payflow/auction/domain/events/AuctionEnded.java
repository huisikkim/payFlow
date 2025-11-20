package com.example.payflow.auction.domain.events;

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
public class AuctionEnded {
    private Long auctionId;
    private String winnerId;
    private BigDecimal finalPrice;
    private Integer totalBids;
    private LocalDateTime timestamp;
}
