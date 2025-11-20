package com.example.payflow.auction.application.dto;

import com.example.payflow.auction.domain.Bid;
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
public class BidResponse {
    
    private Long id;
    private Long auctionId;
    private String bidderId;
    private String bidderName;
    private BigDecimal amount;
    private LocalDateTime bidTime;
    private Boolean isWinning;
    private Boolean isAutoBid;
    
    public static BidResponse from(Bid bid) {
        return BidResponse.builder()
                .id(bid.getId())
                .auctionId(bid.getAuctionId())
                .bidderId(bid.getBidderId())
                .bidderName(bid.getBidderName())
                .amount(bid.getAmount())
                .bidTime(bid.getBidTime())
                .isWinning(bid.getIsWinning())
                .isAutoBid(bid.getIsAutoBid())
                .build();
    }
}
