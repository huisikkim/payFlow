package com.example.payflow.auction.application;

import com.example.payflow.auction.application.dto.BidRequest;
import com.example.payflow.auction.application.dto.BidResponse;
import com.example.payflow.auction.domain.*;
import com.example.payflow.auction.domain.events.BidPlaced;
import com.example.payflow.auction.infrastructure.AuctionEventPublisher;
import com.example.payflow.security.domain.User;
import com.example.payflow.security.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BidService {
    
    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final BidHistoryRepository bidHistoryRepository;
    private final UserRepository userRepository;
    private final AutoBidService autoBidService;
    private final AuctionEventPublisher eventPublisher;
    
    @Transactional
    public BidResponse placeBid(Long auctionId, BidRequest request, String bidderId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("경매를 찾을 수 없습니다."));
        
        User bidder = userRepository.findByUsername(bidderId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 이전 최고 입찰자 저장
        Optional<Bid> previousWinningBid = bidRepository.findWinningBid(auctionId);
        
        // 입찰 생성
        Bid bid = Bid.builder()
                .auctionId(auctionId)
                .bidderId(bidderId)
                .bidderName(bidder.getUsername())
                .amount(request.getAmount())
                .bidTime(LocalDateTime.now())
                .isWinning(true)
                .isAutoBid(false)
                .build();
        
        bid = bidRepository.save(bid);
        
        // 경매 업데이트
        auction.placeBid(request.getAmount(), bidderId, bid.getId());
        
        // 이전 최고 입찰 상태 변경
        if (previousWinningBid.isPresent()) {
            Bid previousBid = previousWinningBid.get();
            previousBid.markAsOutbid();
            
            // 밀린 입찰 이벤트 기록
            BidHistory outbidHistory = BidHistory.bidOutbid(
                    auctionId, 
                    previousBid.getBidderId(), 
                    previousBid.getAmount()
            );
            bidHistoryRepository.save(outbidHistory);
        }
        
        // 입찰 이벤트 기록
        BidHistory bidHistory = BidHistory.bidPlaced(auctionId, bidderId, request.getAmount());
        bidHistoryRepository.save(bidHistory);
        
        // 입찰 이벤트 발행
        BidPlaced event = BidPlaced.builder()
                .auctionId(auctionId)
                .bidId(bid.getId())
                .bidderId(bidderId)
                .amount(request.getAmount())
                .isAutoBid(false)
                .timestamp(LocalDateTime.now())
                .build();
        eventPublisher.publishBidPlaced(event);
        
        log.info("입찰 성공: auctionId={}, bidderId={}, amount={}", 
                auctionId, bidderId, request.getAmount());
        
        // 자동 입찰 트리거
        if (previousWinningBid.isPresent()) {
            Bid prevBid = previousWinningBid.get();
            autoBidService.triggerAutoBid(auctionId, prevBid.getBidderId());
        }
        
        return BidResponse.from(bid);
    }
    
    @Transactional
    public BidResponse buyNow(Long auctionId, String buyerId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("경매를 찾을 수 없습니다."));
        
        User buyer = userRepository.findByUsername(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        if (auction.getBuyNowPrice() == null) {
            throw new IllegalStateException("즉시 구매가가 설정되지 않았습니다.");
        }
        
        // 즉시 구매 입찰 생성
        Bid bid = Bid.builder()
                .auctionId(auctionId)
                .bidderId(buyerId)
                .bidderName(buyer.getUsername())
                .amount(auction.getBuyNowPrice())
                .bidTime(LocalDateTime.now())
                .isWinning(true)
                .isAutoBid(false)
                .build();
        
        bid = bidRepository.save(bid);
        
        // 경매 즉시 종료
        auction.buyNow(buyerId, bid.getId());
        
        // 입찰 이벤트 기록
        BidHistory bidHistory = BidHistory.bidPlaced(auctionId, buyerId, auction.getBuyNowPrice());
        bidHistoryRepository.save(bidHistory);
        
        // 낙찰 이벤트 기록
        BidHistory wonHistory = BidHistory.auctionWon(auctionId, buyerId, auction.getBuyNowPrice());
        bidHistoryRepository.save(wonHistory);
        
        log.info("즉시 구매: auctionId={}, buyerId={}, price={}", 
                auctionId, buyerId, auction.getBuyNowPrice());
        
        return BidResponse.from(bid);
    }
    
    public List<BidResponse> getBidsByAuction(Long auctionId) {
        return bidRepository.findByAuctionIdOrderByAmountDesc(auctionId)
                .stream()
                .map(BidResponse::from)
                .collect(Collectors.toList());
    }
    
    public Page<BidResponse> getBidsByAuctionPaged(Long auctionId, Pageable pageable) {
        return bidRepository.findByAuctionIdOrderByBidTimeDesc(auctionId, pageable)
                .map(BidResponse::from);
    }
    
    public Optional<BidResponse> getTopBid(Long auctionId) {
        return bidRepository.findTopByAuctionIdOrderByAmountDesc(auctionId)
                .map(BidResponse::from);
    }
    
    public Page<BidResponse> getMyBids(String bidderId, Pageable pageable) {
        return bidRepository.findByBidderIdOrderByBidTimeDesc(bidderId, pageable)
                .map(BidResponse::from);
    }
    
    public List<Long> getMyWinningAuctionIds(String bidderId) {
        return bidRepository.findWinningAuctionIds(bidderId);
    }
}
