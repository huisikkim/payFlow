package com.example.payflow.auction.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutoBidRepository extends JpaRepository<AutoBid, Long> {
    
    Optional<AutoBid> findByAuctionIdAndBidderIdAndIsActiveTrue(Long auctionId, String bidderId);
    
    List<AutoBid> findByAuctionIdAndIsActiveTrue(Long auctionId);
    
    List<AutoBid> findByBidderIdAndIsActiveTrue(String bidderId);
    
    boolean existsByAuctionIdAndBidderIdAndIsActiveTrue(Long auctionId, String bidderId);
}
