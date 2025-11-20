package com.example.payflow.auction.infrastructure;

import com.example.payflow.auction.domain.*;
import com.example.payflow.product.domain.Product;
import com.example.payflow.product.domain.ProductRepository;
import com.example.payflow.product.domain.ProductStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(3)
public class AuctionDataInitializer implements CommandLineRunner {
    
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final ProductRepository productRepository;
    
    @Override
    public void run(String... args) {
        if (auctionRepository.count() > 0) {
            log.info("경매 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }
        
        log.info("경매 초기 데이터 생성 시작...");
        
        // 판매 가능한 상품 조회
        List<Product> availableProducts = productRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProductStatus.AVAILABLE)
                .limit(3)
                .toList();
        
        if (availableProducts.isEmpty()) {
            log.warn("경매에 등록할 상품이 없습니다.");
            return;
        }
        
        // 경매 1: 진행 중
        Product product1 = availableProducts.get(0);
        Auction auction1 = Auction.builder()
                .productId(product1.getId())
                .sellerId(String.valueOf(product1.getSellerId()))
                .startPrice(new BigDecimal("50000"))
                .currentPrice(new BigDecimal("65000"))
                .buyNowPrice(new BigDecimal("150000"))
                .minBidIncrement(new BigDecimal("1000"))
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .status(AuctionStatus.ACTIVE)
                .bidCount(3)
                .viewCount(45)
                .build();
        auction1 = auctionRepository.save(auction1);
        product1.updateStatus(ProductStatus.AUCTION_ACTIVE);
        
        // 경매 1 입찰 내역
        Bid bid1_1 = Bid.builder()
                .auctionId(auction1.getId())
                .bidderId("admin")
                .bidderName("admin")
                .amount(new BigDecimal("55000"))
                .bidTime(LocalDateTime.now().minusMinutes(50))
                .isWinning(false)
                .isAutoBid(false)
                .build();
        bidRepository.save(bid1_1);
        
        Bid bid1_2 = Bid.builder()
                .auctionId(auction1.getId())
                .bidderId("user")
                .bidderName("user")
                .amount(new BigDecimal("60000"))
                .bidTime(LocalDateTime.now().minusMinutes(30))
                .isWinning(false)
                .isAutoBid(false)
                .build();
        bidRepository.save(bid1_2);
        
        Bid bid1_3 = Bid.builder()
                .auctionId(auction1.getId())
                .bidderId("admin")
                .bidderName("admin")
                .amount(new BigDecimal("65000"))
                .bidTime(LocalDateTime.now().minusMinutes(10))
                .isWinning(true)
                .isAutoBid(false)
                .build();
        bidRepository.save(bid1_3);
        
        auction1 = auctionRepository.findById(auction1.getId()).get();
        log.info("✅ 경매 1 생성: ID={}, 현재가={}원, 입찰={}회", 
                auction1.getId(), auction1.getCurrentPrice(), auction1.getBidCount());
        
        if (availableProducts.size() > 1) {
            // 경매 2: 마감 임박
            Product product2 = availableProducts.get(1);
            Auction auction2 = Auction.builder()
                    .productId(product2.getId())
                    .sellerId(String.valueOf(product2.getSellerId()))
                    .startPrice(new BigDecimal("30000"))
                    .currentPrice(new BigDecimal("42000"))
                    .buyNowPrice(new BigDecimal("80000"))
                    .minBidIncrement(new BigDecimal("1000"))
                    .startTime(LocalDateTime.now().minusHours(2))
                    .endTime(LocalDateTime.now().plusHours(2))
                    .status(AuctionStatus.ACTIVE)
                    .bidCount(5)
                    .viewCount(78)
                    .build();
            auction2 = auctionRepository.save(auction2);
            product2.updateStatus(ProductStatus.AUCTION_ACTIVE);
            
            log.info("✅ 경매 2 생성: ID={}, 현재가={}원, 마감 임박", 
                    auction2.getId(), auction2.getCurrentPrice());
        }
        
        if (availableProducts.size() > 2) {
            // 경매 3: 예정
            Product product3 = availableProducts.get(2);
            Auction auction3 = Auction.builder()
                    .productId(product3.getId())
                    .sellerId(String.valueOf(product3.getSellerId()))
                    .startPrice(new BigDecimal("100000"))
                    .currentPrice(new BigDecimal("100000"))
                    .buyNowPrice(new BigDecimal("300000"))
                    .minBidIncrement(new BigDecimal("5000"))
                    .startTime(LocalDateTime.now().plusHours(1))
                    .endTime(LocalDateTime.now().plusDays(5))
                    .status(AuctionStatus.SCHEDULED)
                    .bidCount(0)
                    .viewCount(12)
                    .build();
            auctionRepository.save(auction3);
            product3.updateStatus(ProductStatus.AUCTION_ACTIVE);
            
            log.info("✅ 경매 3 생성: ID={}, 시작가={}원, 예정", 
                    auction3.getId(), auction3.getStartPrice());
        }
        
        log.info("경매 초기 데이터 생성 완료!");
    }
}
