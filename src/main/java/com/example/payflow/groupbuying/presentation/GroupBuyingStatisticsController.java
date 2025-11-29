package com.example.payflow.groupbuying.presentation;

import com.example.payflow.groupbuying.application.GroupBuyingStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 공동구매 통계 API
 */
@RestController
@RequestMapping("/api/group-buying/statistics")
@RequiredArgsConstructor
@Slf4j
public class GroupBuyingStatisticsController {
    
    private final GroupBuyingStatisticsService statisticsService;
    
    /**
     * 유통업자 통계
     */
    @GetMapping("/distributor/{distributorId}")
    public ResponseEntity<Map<String, Object>> getDistributorStatistics(@PathVariable String distributorId) {
        return ResponseEntity.ok(statisticsService.getDistributorStatistics(distributorId));
    }
    
    /**
     * 가게 통계
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<Map<String, Object>> getStoreStatistics(@PathVariable String storeId) {
        return ResponseEntity.ok(statisticsService.getStoreStatistics(storeId));
    }
    
    /**
     * 전체 시스템 통계
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemStatistics() {
        return ResponseEntity.ok(statisticsService.getSystemStatistics());
    }
}
