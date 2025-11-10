package com.example.payflow.logging.application;

import com.example.payflow.logging.application.dto.*;
import com.example.payflow.logging.domain.EventLogRepository;
import com.example.payflow.logging.domain.EventStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 로그 분석 서비스
 * 실시간 메트릭 및 통계 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LogAnalyticsService {
    
    private final EventLogRepository eventLogRepository;
    
    /**
     * 대시보드 메트릭 조회
     */
    @Transactional(readOnly = true)
    public DashboardMetrics getDashboardMetrics(int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        
        // 시간대별 이벤트 건수
        List<Object[]> hourlyData = eventLogRepository.countEventsByHour(startTime);
        List<TimeSeriesData> hourlyCounts = hourlyData.stream()
            .map(row -> new TimeSeriesData((String) row[0], ((Number) row[1]).longValue()))
            .collect(Collectors.toList());
        
        // 이벤트 타입별 건수
        List<Object[]> typeData = eventLogRepository.countEventsByType(startTime);
        List<EventTypeCount> typeCounts = typeData.stream()
            .map(row -> new EventTypeCount((String) row[0], ((Number) row[1]).longValue()))
            .collect(Collectors.toList());
        
        // 서비스별 성공률
        List<Object[]> successRateData = eventLogRepository.calculateSuccessRateByService(startTime);
        List<ServiceSuccessRate> successRates = successRateData.stream()
            .map(row -> new ServiceSuccessRate((String) row[0], ((Number) row[1]).doubleValue()))
            .collect(Collectors.toList());
        
        // 평균 처리 시간
        List<Object[]> processingTimeData = eventLogRepository.calculateAvgProcessingTime(startTime);
        List<ServiceProcessingTime> processingTimes = processingTimeData.stream()
            .map(row -> new ServiceProcessingTime((String) row[0], ((Number) row[1]).doubleValue()))
            .collect(Collectors.toList());
        
        // 실패 이벤트 건수
        long failedCount = eventLogRepository.findByStatusOrderByTimestampDesc(EventStatus.FAILED)
            .stream()
            .filter(e -> e.getTimestamp().isAfter(startTime))
            .count();
        
        return DashboardMetrics.builder()
            .timeRange(hours + " hours")
            .totalEvents(hourlyCounts.stream().mapToLong(TimeSeriesData::getCount).sum())
            .failedEvents(failedCount)
            .hourlyEventCounts(hourlyCounts)
            .eventTypeCounts(typeCounts)
            .serviceSuccessRates(successRates)
            .serviceProcessingTimes(processingTimes)
            .generatedAt(LocalDateTime.now())
            .build();
    }
    
    /**
     * 실시간 이벤트 스트림 (최근 N개)
     */
    @Transactional(readOnly = true)
    public List<RecentEventDto> getRecentEvents(int limit) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        
        return eventLogRepository.findByTimestampBetweenOrderByTimestampDesc(
            startTime, LocalDateTime.now()
        ).stream()
            .limit(limit)
            .map(event -> RecentEventDto.builder()
                .eventType(event.getEventType())
                .serviceName(event.getServiceName())
                .status(event.getStatus().name())
                .timestamp(event.getTimestamp())
                .correlationId(event.getCorrelationId())
                .build())
            .collect(Collectors.toList());
    }
}
