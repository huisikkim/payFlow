package com.example.payflow.sessionreplay.infrastructure;

import com.example.payflow.sessionreplay.domain.Session;
import com.example.payflow.sessionreplay.domain.SessionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 세션 JPA Repository 구현
 */
@Repository
public interface JpaSessionRepository extends JpaRepository<Session, String>, SessionRepository {
    
    @Override
    Page<Session> findByUserId(String userId, Pageable pageable);
    
    @Override
    Page<Session> findByStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    @Override
    @Modifying
    @Transactional
    @Query("DELETE FROM Session s WHERE s.startTime < :cutoffDate")
    void deleteByStartTimeBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}
