package com.example.payflow.logging.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentEventDto {
    private String eventType;
    private String serviceName;
    private String status;
    private LocalDateTime timestamp;
    private String correlationId;
}
