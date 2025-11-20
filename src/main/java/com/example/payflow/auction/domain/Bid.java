package com.example.payflow.auction.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long auctionId;
    
    @Column(nullable = false)
    private String bidderId;
    
    @Column(nullable = false)
    private String bidderName;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private LocalDateTime bidTime;
    
    @Builder.Default
    private Boolean isWinning = false;
    
    @Builder.Default
    private Boolean isAutoBid = false;
    
    @PrePersist
    protected void onCreate() {
        if (bidTime == null) {
            bidTime = LocalDateTime.now();
        }
    }
    
    public void markAsWinning() {
        this.isWinning = true;
    }
    
    public void markAsOutbid() {
        this.isWinning = false;
    }
}
