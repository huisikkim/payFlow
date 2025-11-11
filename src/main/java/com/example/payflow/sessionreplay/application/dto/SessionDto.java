package com.example.payflow.sessionreplay.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 세션 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDto {
    
    private String sessionId;
    private String userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalEvents;
    private String deviceInfo;
    private Long durationSeconds;
}
