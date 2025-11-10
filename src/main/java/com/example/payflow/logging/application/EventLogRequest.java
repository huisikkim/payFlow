package com.example.payflow.logging.application;

import com.example.payflow.logging.domain.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventLogRequest {
    private String correlationId;
    private String eventType;
    private String serviceName;
    private Object payload;
    private String userId;
    private EventStatus status;
    private String errorMessage;
    private Long processingTimeMs;
}
