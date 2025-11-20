package com.example.payflow.auction.infrastructure;

import com.example.payflow.auction.application.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionScheduler {
    
    private final AuctionService auctionService;
    
    /**
     * 1분마다 예정된 경매 시작
     */
    @Scheduled(fixedRate = 60000)
    public void startScheduledAuctions() {
        try {
            auctionService.startScheduledAuctions();
        } catch (Exception e) {
            log.error("예정된 경매 시작 중 오류 발생", e);
        }
    }
    
    /**
     * 1분마다 만료된 경매 종료
     */
    @Scheduled(fixedRate = 60000)
    public void closeExpiredAuctions() {
        try {
            auctionService.closeExpiredAuctions();
        } catch (Exception e) {
            log.error("만료된 경매 종료 중 오류 발생", e);
        }
    }
}
