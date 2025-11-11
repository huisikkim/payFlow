package com.example.payflow.sessionreplay.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 세션 필터 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionFilterDto {
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String userId;
    private Integer pageSize;
}
