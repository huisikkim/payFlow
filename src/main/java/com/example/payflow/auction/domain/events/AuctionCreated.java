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
public class AuctionCreated {
    private Long auctionId;
    private Long productId;
    private String sellerId;
    private BigDecimal startPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime timestamp;
}
