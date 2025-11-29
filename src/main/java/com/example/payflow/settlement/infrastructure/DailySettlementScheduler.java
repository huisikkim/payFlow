package com.example.payflow.settlement.infrastructure;

import com.example.payflow.settlement.application.DailySettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailySettlementScheduler {
    
    private final DailySettlementService dailySettlementService;
    
    /**
     * 매일 자정 1시에 전날 정산 재집계
     * 실시간으로 집계되지만, 혹시 누락된 건이 있을 수 있어 재집계
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void recalculateYesterdaySettlement() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        
        log.info("⏰ [스케줄러] 일일 정산 재집계 시작: {}", yesterday);
        
        try {
            dailySettlementService.recalculateDailySettlement(yesterday);
            log.info("✅ [스케줄러] 일일 정산 재집계 완료: {}", yesterday);
        } catch (Exception e) {
            log.error("❌ [스케줄러] 일일 정산 재집계 실패: {}", yesterday, e);
        }
    }
    
    /**
     * 매주 월요일 오전 2시에 지난주 정산 재집계
     */
    @Scheduled(cron = "0 0 2 * * MON")
    public void recalculateLastWeekSettlement() {
        LocalDate today = LocalDate.now();
        LocalDate lastMonday = today.minusWeeks(1).with(java.time.DayOfWeek.MONDAY);
        LocalDate lastSunday = lastMonday.plusDays(6);
        
        log.info("⏰ [스케줄러] 주간 정산 재집계 시작: {} ~ {}", lastMonday, lastSunday);
        
        try {
            for (LocalDate date = lastMonday; !date.isAfter(lastSunday); date = date.plusDays(1)) {
                dailySettlementService.recalculateDailySettlement(date);
            }
            log.info("✅ [스케줄러] 주간 정산 재집계 완료: {} ~ {}", lastMonday, lastSunday);
        } catch (Exception e) {
            log.error("❌ [스케줄러] 주간 정산 재집계 실패", e);
        }
    }
}
