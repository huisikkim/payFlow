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
public class BidPlaced {
    private Long auctionId;
    private Long bidId;
    private String bidderId;
    private BigDecimal amount;
    private Boolean isAutoBid;
    private LocalDateTime timestamp;
}
