package com.example.payflow.auction.application;

import com.example.payflow.auction.application.dto.AuctionCreateRequest;
import com.example.payflow.auction.application.dto.AuctionResponse;
import com.example.payflow.auction.domain.*;
import com.example.payflow.auction.domain.events.AuctionCreated;
import com.example.payflow.auction.domain.events.AuctionEnded;
import com.example.payflow.auction.infrastructure.AuctionEventPublisher;
import com.example.payflow.product.domain.Product;
import com.example.payflow.product.domain.ProductRepository;
import com.example.payflow.product.domain.ProductStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuctionService {
    
    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final BidRepository bidRepository;
    private final BidHistoryRepository bidHistoryRepository;
    private final AuctionEventPublisher eventPublisher;
    
    @Transactional
    public AuctionResponse createAuction(AuctionCreateRequest request, String sellerId) {
        // 상품 검증
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        
        if (!product.getSellerId().equals(sellerId)) {
            throw new IllegalArgumentException("본인의 상품만 경매에 등록할 수 있습니다.");
        }
        
        if (product.getStatus() != ProductStatus.AVAILABLE) {
            throw new IllegalStateException("판매 가능한 상품만 경매에 등록할 수 있습니다.");
        }
        
        // 이미 경매 중인지 확인
        if (auctionRepository.findByProductId(product.getId()).isPresent()) {
            throw new IllegalStateException("이미 경매 중인 상품입니다.");
        }
        
        // 시간 검증
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 늦어야 합니다.");
        }
        
        // 즉시 구매가 검증
        if (request.getBuyNowPrice() != null && 
            request.getBuyNowPrice().compareTo(request.getStartPrice()) <= 0) {
            throw new IllegalArgumentException("즉시 구매가는 시작가보다 높아야 합니다.");
        }
        
        // 경매 생성
        AuctionStatus initialStatus = request.getStartTime().isAfter(LocalDateTime.now()) 
                ? AuctionStatus.SCHEDULED 
                : AuctionStatus.ACTIVE;
        
        Auction auction = Auction.builder()
                .productId(product.getId())
                .sellerId(sellerId)
                .startPrice(request.getStartPrice())
                .currentPrice(request.getStartPrice())
                .buyNowPrice(request.getBuyNowPrice())
                .minBidIncrement(request.getMinBidIncrement())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(initialStatus)
                .bidCount(0)
                .viewCount(0)
                .build();
        
        auction = auctionRepository.save(auction);
        
        // 상품 상태 변경
        product.updateStatus(ProductStatus.AUCTION_ACTIVE);
        
        log.info("경매 생성: auctionId={}, productId={}, sellerId={}", 
                auction.getId(), product.getId(), sellerId);
        
        // 이벤트 발행
        AuctionCreated event = AuctionCreated.builder()
                .auctionId(auction.getId())
                .productId(product.getId())
                .sellerId(sellerId)
                .startPrice(auction.getStartPrice())
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .timestamp(LocalDateTime.now())
                .build();
        eventPublisher.publishAuctionCreated(event);
        
        return AuctionResponse.from(auction);
    }
    
    public AuctionResponse getAuction(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("경매를 찾을 수 없습니다."));
        return AuctionResponse.from(auction);
    }
    
    @Transactional
    public AuctionResponse getAuctionWithViewCount(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("경매를 찾을 수 없습니다."));
        auction.incrementViewCount();
        return AuctionResponse.from(auction);
    }
    
    public Page<AuctionResponse> getActiveAuctions(Pageable pageable) {
        return auctionRepository.findByStatus(AuctionStatus.ACTIVE, pageable)
                .map(AuctionResponse::from);
    }
    
    public Page<AuctionResponse> getScheduledAuctions(Pageable pageable) {
        return auctionRepository.findByStatus(AuctionStatus.SCHEDULED, pageable)
                .map(AuctionResponse::from);
    }
    
    public Page<AuctionResponse> getEndedAuctions(Pageable pageable) {
        return auctionRepository.findByStatus(AuctionStatus.ENDED, pageable)
                .map(AuctionResponse::from);
    }
    
    public Page<AuctionResponse> getPopularAuctions(Pageable pageable) {
        return auctionRepository.findPopularAuctions(AuctionStatus.ACTIVE, pageable)
                .map(AuctionResponse::from);
    }
    
    public Page<AuctionResponse> getEndingSoonAuctions(Pageable pageable) {
        return auctionRepository.findEndingSoonAuctions(AuctionStatus.ACTIVE, pageable)
                .map(AuctionResponse::from);
    }
    
    public Page<AuctionResponse> getMySellingAuctions(String sellerId, Pageable pageable) {
        return auctionRepository.findBySellerId(sellerId, pageable)
                .map(AuctionResponse::from);
    }
    
    public Page<AuctionResponse> getMyWinningAuctions(String userId, Pageable pageable) {
        return auctionRepository.findWonAuctions(userId, pageable)
                .map(AuctionResponse::from);
    }
    
    @Transactional
    public void cancelAuction(Long auctionId, String sellerId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("경매를 찾을 수 없습니다."));
        
        if (!auction.getSellerId().equals(sellerId)) {
            throw new IllegalArgumentException("본인의 경매만 취소할 수 있습니다.");
        }
        
        auction.cancel();
        
        // 상품 상태 복원
        Product product = productRepository.findById(auction.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        product.updateStatus(ProductStatus.AVAILABLE);
        
        log.info("경매 취소: auctionId={}, sellerId={}", auctionId, sellerId);
    }
    
    @Transactional
    public void endAuction(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("경매를 찾을 수 없습니다."));
        
        auction.end();
        
        // 낙찰자가 있으면 상품 상태 변경
        if (auction.hasWinner()) {
            Product product = productRepository.findById(auction.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
            product.updateStatus(ProductStatus.SOLD);
            
            // 낙찰 이벤트 기록
            BidHistory wonHistory = BidHistory.auctionWon(
                    auction.getId(), 
                    auction.getWinnerId(), 
                    auction.getCurrentPrice()
            );
            bidHistoryRepository.save(wonHistory);
            
            // 경매 종료 이벤트 발행
            AuctionEnded event = AuctionEnded.builder()
                    .auctionId(auction.getId())
                    .winnerId(auction.getWinnerId())
                    .finalPrice(auction.getCurrentPrice())
                    .totalBids(auction.getBidCount())
                    .timestamp(LocalDateTime.now())
                    .build();
            eventPublisher.publishAuctionEnded(event);
            
            log.info("경매 종료 - 낙찰: auctionId={}, winnerId={}, price={}", 
                    auctionId, auction.getWinnerId(), auction.getCurrentPrice());
        } else {
            // 낙찰자 없으면 상품 상태 복원
            Product product = productRepository.findById(auction.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
            product.updateStatus(ProductStatus.AVAILABLE);
            
            log.info("경매 종료 - 유찰: auctionId={}", auctionId);
        }
    }
    
    @Transactional
    public void startScheduledAuctions() {
        List<Auction> scheduledAuctions = auctionRepository.findByStatus(AuctionStatus.SCHEDULED);
        LocalDateTime now = LocalDateTime.now();
        
        for (Auction auction : scheduledAuctions) {
            if (!auction.getStartTime().isAfter(now)) {
                auction.start();
                log.info("예정된 경매 시작: auctionId={}", auction.getId());
            }
        }
    }
    
    @Transactional
    public void closeExpiredAuctions() {
        List<Auction> expiredAuctions = auctionRepository
                .findByStatusAndEndTimeBefore(AuctionStatus.ACTIVE, LocalDateTime.now());
        
        for (Auction auction : expiredAuctions) {
            endAuction(auction.getId());
        }
        
        if (!expiredAuctions.isEmpty()) {
            log.info("만료된 경매 종료: count={}", expiredAuctions.size());
        }
    }
}
