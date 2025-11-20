package com.example.payflow.auction.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    
    List<Bid> findByAuctionIdOrderByAmountDesc(Long auctionId);
    
    Page<Bid> findByAuctionIdOrderByBidTimeDesc(Long auctionId, Pageable pageable);
    
    Optional<Bid> findTopByAuctionIdOrderByAmountDesc(Long auctionId);
    
    List<Bid> findByBidderId(String bidderId);
    
    Page<Bid> findByBidderIdOrderByBidTimeDesc(String bidderId, Pageable pageable);
    
    int countByAuctionId(Long auctionId);
    
    @Query("SELECT b FROM Bid b WHERE b.auctionId = :auctionId AND b.isWinning = true")
    Optional<Bid> findWinningBid(Long auctionId);
    
    @Query("SELECT DISTINCT b.auctionId FROM Bid b WHERE b.bidderId = :bidderId AND b.isWinning = true")
    List<Long> findWinningAuctionIds(String bidderId);
    
    boolean existsByAuctionIdAndBidderId(Long auctionId, String bidderId);
}
