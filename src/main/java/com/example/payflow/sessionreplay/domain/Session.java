package com.example.payflow.sessionreplay.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 세션 Aggregate Root
 * 사용자의 웹사이트 방문 세션을 나타냄
 */
@Entity
@Table(name = "sessions", indexes = {
    @Index(name = "idx_session_user_id", columnList = "userId"),
    @Index(name = "idx_session_start_time", columnList = "startTime")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {
    
    @Id
    private String sessionId; // UUID
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer totalEvents = 0;
    
    @Column(columnDefinition = "TEXT")
    private String deviceInfo; // JSON: browser, OS, screen size
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    /**
     * 이벤트 추가 시 호출
     */
    public void addEvent() {
        this.totalEvents++;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 세션 종료
     */
    public void endSession() {
        this.endTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 세션 지속 시간 계산
     */
    public Duration getDuration() {
        if (endTime == null) {
            return Duration.between(startTime, LocalDateTime.now());
        }
        return Duration.between(startTime, endTime);
    }
}
