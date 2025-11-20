package com.example.payflow.auction.infrastructure;

import com.example.payflow.auction.domain.events.AuctionCreated;
import com.example.payflow.auction.domain.events.AuctionEnded;
import com.example.payflow.auction.domain.events.BidPlaced;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionEventPublisher {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    public void publishAuctionCreated(AuctionCreated event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("AuctionCreated", message);
            log.info("📨 경매 생성 이벤트 발행: auctionId={}", event.getAuctionId());
        } catch (Exception e) {
            log.error("경매 생성 이벤트 발행 실패", e);
        }
    }
    
    public void publishBidPlaced(BidPlaced event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("BidPlaced", message);
            log.info("📨 입찰 이벤트 발행: auctionId={}, bidderId={}, amount={}", 
                    event.getAuctionId(), event.getBidderId(), event.getAmount());
        } catch (Exception e) {
            log.error("입찰 이벤트 발행 실패", e);
        }
    }
    
    public void publishAuctionEnded(AuctionEnded event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("AuctionEnded", message);
            log.info("📨 경매 종료 이벤트 발행: auctionId={}, winnerId={}, finalPrice={}", 
                    event.getAuctionId(), event.getWinnerId(), event.getFinalPrice());
        } catch (Exception e) {
            log.error("경매 종료 이벤트 발행 실패", e);
        }
    }
}
