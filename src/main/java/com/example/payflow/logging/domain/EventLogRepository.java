package com.example.payflow.logging.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventLogRepository extends JpaRepository<EventLog, Long> {
    
    /**
     * Correlation ID로 전체 이벤트 체인 조회
     */
    List<EventLog> findByCorrelationIdOrderByTimestampAsc(String correlationId);
    
    /**
     * 이벤트 타입별 조회
     */
    List<EventLog> findByEventTypeOrderByTimestampDesc(String eventType);
    
    /**
     * 사용자별 이벤트 조회
     */
    List<EventLog> findByUserIdOrderByTimestampDesc(String userId);
    
    /**
     * 시간 범위 내 이벤트 조회
     */
    List<EventLog> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime start, 
        LocalDateTime end
    );
    
    /**
     * 실패한 이벤트 조회
     */
    List<EventLog> findByStatusOrderByTimestampDesc(EventStatus status);
    
    /**
     * 시간대별 이벤트 건수 집계
     */
    @Query("SELECT FORMATDATETIME(e.timestamp, 'yyyy-MM-dd HH:00:00') as hour, " +
           "COUNT(e) as count " +
           "FROM EventLog e " +
           "WHERE e.timestamp >= :startTime " +
           "GROUP BY FORMATDATETIME(e.timestamp, 'yyyy-MM-dd HH:00:00') " +
           "ORDER BY hour DESC")
    List<Object[]> countEventsByHour(@Param("startTime") LocalDateTime startTime);
    
    /**
     * 이벤트 타입별 건수 집계
     */
    @Query("SELECT e.eventType, COUNT(e) " +
           "FROM EventLog e " +
           "WHERE e.timestamp >= :startTime " +
           "GROUP BY e.eventType " +
           "ORDER BY COUNT(e) DESC")
    List<Object[]> countEventsByType(@Param("startTime") LocalDateTime startTime);
    
    /**
     * 서비스별 성공률 계산
     */
    @Query("SELECT e.serviceName, " +
           "SUM(CASE WHEN e.status = 'SUCCESS' THEN 1 ELSE 0 END) * 100.0 / COUNT(e) as successRate " +
           "FROM EventLog e " +
           "WHERE e.timestamp >= :startTime " +
           "GROUP BY e.serviceName")
    List<Object[]> calculateSuccessRateByService(@Param("startTime") LocalDateTime startTime);
    
    /**
     * 평균 처리 시간 계산
     */
    @Query("SELECT e.serviceName, AVG(e.processingTimeMs) " +
           "FROM EventLog e " +
           "WHERE e.timestamp >= :startTime AND e.processingTimeMs IS NOT NULL " +
           "GROUP BY e.serviceName")
    List<Object[]> calculateAvgProcessingTime(@Param("startTime") LocalDateTime startTime);
}
