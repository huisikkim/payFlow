package com.example.payflow.groupbuying.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 공동구매 스케줄러
 * - 만료된 방 자동 마감
 * - 마감된 방 주문 자동 생성
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GroupBuyingScheduler {
    
    private final GroupBuyingRoomService roomService;
    private final GroupBuyingOrderService orderService;
    
    /**
     * 만료된 방 자동 마감
     * 매 5분마다 실행
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void closeExpiredRooms() {
        try {
            log.info("만료된 공동구매 방 자동 마감 시작");
            roomService.closeExpiredRooms();
            log.info("만료된 공동구매 방 자동 마감 완료");
        } catch (Exception e) {
            log.error("만료된 공동구매 방 자동 마감 실패", e);
        }
    }
    
    /**
     * 성공적으로 마감된 방의 주문 자동 생성
     * 매 10분마다 실행
     */
    @Scheduled(cron = "0 */10 * * * *")
    public void createOrdersForSuccessfulRooms() {
        try {
            log.info("공동구매 주문 자동 생성 시작");
            orderService.createOrdersForSuccessfulRooms();
            log.info("공동구매 주문 자동 생성 완료");
        } catch (Exception e) {
            log.error("공동구매 주문 자동 생성 실패", e);
        }
    }
}
