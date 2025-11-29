package com.example.payflow.settlement.presentation;

import com.example.payflow.settlement.application.DailySettlementService;
import com.example.payflow.settlement.presentation.dto.DailySettlementResponse;
import com.example.payflow.settlement.presentation.dto.SettlementStatisticsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/daily-settlements")
@RequiredArgsConstructor
@Slf4j
public class DailySettlementController {
    
    private final DailySettlementService dailySettlementService;
    
    /**
     * 가게별 일일 정산 조회
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<DailySettlementResponse>> getStoreDaily(
            @PathVariable String storeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // 기본값: 최근 30일
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        log.info("📊 가게 일일 정산 조회: storeId={}, period={} ~ {}", storeId, startDate, endDate);
        
        List<DailySettlementResponse> settlements = 
            dailySettlementService.getDailySettlementsByStore(storeId, startDate, endDate);
        
        return ResponseEntity.ok(settlements);
    }
    
    /**
     * 유통업자별 일일 정산 조회
     */
    @GetMapping("/distributor/{distributorId}")
    public ResponseEntity<List<DailySettlementResponse>> getDistributorDaily(
            @PathVariable String distributorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // 기본값: 최근 30일
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        log.info("📊 유통업자 일일 정산 조회: distributorId={}, period={} ~ {}", 
            distributorId, startDate, endDate);
        
        List<DailySettlementResponse> settlements = 
            dailySettlementService.getDailySettlementsByDistributor(distributorId, startDate, endDate);
        
        return ResponseEntity.ok(settlements);
    }
    
    /**
     * 가게별 정산 통계
     */
    @GetMapping("/store/{storeId}/statistics")
    public ResponseEntity<SettlementStatisticsResponse> getStoreStatistics(
            @PathVariable String storeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // 기본값: 이번 달
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        log.info("📈 가게 정산 통계 조회: storeId={}, period={} ~ {}", storeId, startDate, endDate);
        
        SettlementStatisticsResponse statistics = 
            dailySettlementService.getStoreStatistics(storeId, startDate, endDate);
        
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 유통업자별 정산 통계
     */
    @GetMapping("/distributor/{distributorId}/statistics")
    public ResponseEntity<SettlementStatisticsResponse> getDistributorStatistics(
            @PathVariable String distributorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // 기본값: 이번 달
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        log.info("📈 유통업자 정산 통계 조회: distributorId={}, period={} ~ {}", 
            distributorId, startDate, endDate);
        
        SettlementStatisticsResponse statistics = 
            dailySettlementService.getDistributorStatistics(distributorId, startDate, endDate);
        
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 특정 날짜 정산 재집계 (관리자용)
     */
    @PostMapping("/recalculate")
    public ResponseEntity<Void> recalculateSettlement(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {
        
        log.info("🔄 정산 재집계 요청: targetDate={}", targetDate);
        
        dailySettlementService.recalculateDailySettlement(targetDate);
        
        return ResponseEntity.ok().build();
    }
}
