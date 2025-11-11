package com.example.payflow.sessionreplay.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 세션 Repository 인터페이스
 */
public interface SessionRepository {
    
    Session save(Session session);
    
    Optional<Session> findById(String sessionId);
    
    Page<Session> findAll(Pageable pageable);
    
    Page<Session> findByUserId(String userId, Pageable pageable);
    
    Page<Session> findByStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    void deleteByStartTimeBefore(LocalDateTime cutoffDate);
}
