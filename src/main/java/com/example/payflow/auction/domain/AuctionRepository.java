package com.example.payflow.auction.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    
    List<Auction> findByStatus(AuctionStatus status);
    
    Page<Auction> findByStatus(AuctionStatus status, Pageable pageable);
    
    List<Auction> findByStatusAndEndTimeBefore(AuctionStatus status, LocalDateTime time);
    
    Optional<Auction> findByProductId(Long productId);
    
    List<Auction> findBySellerId(String sellerId);
    
    Page<Auction> findBySellerId(String sellerId, Pageable pageable);
    
    @Query("SELECT a FROM Auction a WHERE a.status = :status ORDER BY a.bidCount DESC")
    Page<Auction> findPopularAuctions(AuctionStatus status, Pageable pageable);
    
    @Query("SELECT a FROM Auction a WHERE a.status = :status ORDER BY a.endTime ASC")
    Page<Auction> findEndingSoonAuctions(AuctionStatus status, Pageable pageable);
    
    @Query("SELECT a FROM Auction a WHERE a.winnerId = :userId AND a.status = 'ENDED'")
    Page<Auction> findWonAuctions(String userId, Pageable pageable);
    
    @Query("SELECT COUNT(a) FROM Auction a WHERE a.status = :status")
    long countByStatus(AuctionStatus status);
}
