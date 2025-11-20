package com.example.payflow.auction.application.dto;

import com.example.payflow.auction.domain.Auction;
import com.example.payflow.auction.domain.AuctionStatus;
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
public class AuctionResponse {
    
    private Long id;
    private Long productId;
    private String sellerId;
    private BigDecimal startPrice;
    private BigDecimal currentPrice;
    private BigDecimal buyNowPrice;
    private BigDecimal minBidIncrement;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionStatus status;
    private String winnerId;
    private Long winningBidId;
    private Integer bidCount;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static AuctionResponse from(Auction auction) {
        return AuctionResponse.builder()
                .id(auction.getId())
                .productId(auction.getProductId())
                .sellerId(auction.getSellerId())
                .startPrice(auction.getStartPrice())
                .currentPrice(auction.getCurrentPrice())
                .buyNowPrice(auction.getBuyNowPrice())
                .minBidIncrement(auction.getMinBidIncrement())
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .status(auction.getStatus())
                .winnerId(auction.getWinnerId())
                .winningBidId(auction.getWinningBidId())
                .bidCount(auction.getBidCount())
                .viewCount(auction.getViewCount())
                .createdAt(auction.getCreatedAt())
                .updatedAt(auction.getUpdatedAt())
                .build();
    }
}
