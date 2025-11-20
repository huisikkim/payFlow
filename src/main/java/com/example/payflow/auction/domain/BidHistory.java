package com.example.payflow.auction.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bid_history")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long auctionId;
    
    @Column(nullable = false)
    private String bidderId;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, length = 50)
    private String eventType;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
    
    public static BidHistory bidPlaced(Long auctionId, String bidderId, BigDecimal amount) {
        return BidHistory.builder()
                .auctionId(auctionId)
                .bidderId(bidderId)
                .amount(amount)
                .eventType("BID_PLACED")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static BidHistory bidOutbid(Long auctionId, String bidderId, BigDecimal amount) {
        return BidHistory.builder()
                .auctionId(auctionId)
                .bidderId(bidderId)
                .amount(amount)
                .eventType("BID_OUTBID")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static BidHistory auctionWon(Long auctionId, String bidderId, BigDecimal amount) {
        return BidHistory.builder()
                .auctionId(auctionId)
                .bidderId(bidderId)
                .amount(amount)
                .eventType("AUCTION_WON")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
