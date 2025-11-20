package com.example.payflow.auction.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidHistoryRepository extends JpaRepository<BidHistory, Long> {
    
    List<BidHistory> findByAuctionIdOrderByTimestampDesc(Long auctionId);
    
    Page<BidHistory> findByAuctionIdOrderByTimestampDesc(Long auctionId, Pageable pageable);
    
    List<BidHistory> findByBidderIdOrderByTimestampDesc(String bidderId);
    
    List<BidHistory> findByEventType(String eventType);
}
