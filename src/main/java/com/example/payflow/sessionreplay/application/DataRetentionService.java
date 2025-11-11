package com.example.payflow.sessionreplay.application;

import com.example.payflow.sessionreplay.domain.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 데이터 보관 정책 서비스
 * 오래된 세션 데이터를 자동으로 삭제
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataRetentionService {
    
    private final SessionRepository sessionRepository;
    
    @Value("${session-replay.retention-days:30}")
    private int retentionDays;
    
    /**
     * 매일 새벽 2시에 오래된 세션 삭제
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldSessions() {
        log.info("Starting data retention cleanup job...");
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        
        try {
            sessionRepository.deleteByStartTimeBefore(cutoffDate);
            log.info("Successfully deleted sessions older than {} (retention: {} days)", 
                cutoffDate, retentionDays);
        } catch (Exception e) {
            log.error("Failed to cleanup old sessions", e);
        }
    }
}
