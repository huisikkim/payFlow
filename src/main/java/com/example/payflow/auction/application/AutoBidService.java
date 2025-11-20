package com.example.payflow.auction.application;

import com.example.payflow.auction.application.dto.AutoBidRequest;
import com.example.payflow.auction.application.dto.AutoBidResponse;
import com.example.payflow.auction.domain.*;
import com.example.payflow.security.domain.User;
import com.example.payflow.security.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AutoBidService {
    
    private final AutoBidRepository autoBidRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final BidHistoryRepository bidHistoryRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public AutoBidResponse createAutoBid(Long auctionId, AutoBidRequest request, String bidderId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("경매를 찾을 수 없습니다."));
        
        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new IllegalStateException("진행 중인 경매에만 자동 입찰을 설정할 수 있습니다.");
        }
        
        if (auction.getSellerId().equals(bidderId)) {
            throw new IllegalArgumentException("본인의 경매에는 자동 입찰을 설정할 수 없습니다.");
        }
        
        // 기존 자동 입찰 비활성화
        Optional<AutoBid> existingAutoBid = autoBidRepository
                .findByAuctionIdAndBidderIdAndIsActiveTrue(auctionId, bidderId);
        existingAutoBid.ifPresent(AutoBid::deactivate);
        
        // 새 자동 입찰 생성
        AutoBid autoBid = AutoBid.builder()
                .auctionId(auctionId)
                .bidderId(bidderId)
                .maxAmount(request.getMaxAmount())
                .isActive(true)
                .build();
        
        autoBid = autoBidRepository.save(autoBid);
        
        log.info("자동 입찰 설정: auctionId={}, bidderId={}, maxAmount={}", 
                auctionId, bidderId, request.getMaxAmount());
        
        return AutoBidResponse.from(autoBid);
    }
    
    @Transactional
    public void cancelAutoBid(Long auctionId, String bidderId) {
        AutoBid autoBid = autoBidRepository
                .findByAuctionIdAndBidderIdAndIsActiveTrue(auctionId, bidderId)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 자동 입찰을 찾을 수 없습니다."));
        
        autoBid.deactivate();
        
        log.info("자동 입찰 취소: auctionId={}, bidderId={}", auctionId, bidderId);
    }
    
    public Optional<AutoBidResponse> getAutoBid(Long auctionId, String bidderId) {
        return autoBidRepository
                .findByAuctionIdAndBidderIdAndIsActiveTrue(auctionId, bidderId)
                .map(AutoBidResponse::from);
    }
    
    @Transactional
    public void triggerAutoBid(Long auctionId, String outbidBidderId) {
        // 밀린 입찰자의 자동 입찰 설정 확인
        Optional<AutoBid> autoBidOpt = autoBidRepository
                .findByAuctionIdAndBidderIdAndIsActiveTrue(auctionId, outbidBidderId);
        
        if (autoBidOpt.isEmpty()) {
            return;
        }
        
        AutoBid autoBid = autoBidOpt.get();
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("경매를 찾을 수 없습니다."));
        
        // 다음 입찰 금액 계산
        BigDecimal nextBidAmount = auction.getCurrentPrice().add(auction.getMinBidIncrement());
        
        // 최대 금액 확인
        if (!autoBid.canBid(nextBidAmount)) {
            log.info("자동 입찰 최대 금액 도달: auctionId={}, bidderId={}, maxAmount={}", 
                    auctionId, outbidBidderId, autoBid.getMaxAmount());
            autoBid.deactivate();
            return;
        }
        
        // 자동 입찰 실행
        User bidder = userRepository.findByUsername(outbidBidderId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        Bid bid = Bid.builder()
                .auctionId(auctionId)
                .bidderId(outbidBidderId)
                .bidderName(bidder.getUsername())
                .amount(nextBidAmount)
                .bidTime(LocalDateTime.now())
                .isWinning(true)
                .isAutoBid(true)
                .build();
        
        bid = bidRepository.save(bid);
        
        // 이전 최고 입찰 상태 변경
        Optional<Bid> previousWinningBid = bidRepository.findWinningBid(auctionId);
        previousWinningBid.ifPresent(Bid::markAsOutbid);
        
        // 경매 업데이트
        auction.placeBid(nextBidAmount, outbidBidderId, bid.getId());
        
        // 입찰 이벤트 기록
        BidHistory bidHistory = BidHistory.bidPlaced(auctionId, outbidBidderId, nextBidAmount);
        bidHistoryRepository.save(bidHistory);
        
        log.info("자동 입찰 실행: auctionId={}, bidderId={}, amount={}", 
                auctionId, outbidBidderId, nextBidAmount);
    }
}
