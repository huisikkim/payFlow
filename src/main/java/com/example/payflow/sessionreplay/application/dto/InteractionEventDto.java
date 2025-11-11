package com.example.payflow.sessionreplay.application.dto;

import com.example.payflow.sessionreplay.domain.EventType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 상호작용 이벤트 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InteractionEventDto {
    
    @NotNull(message = "sessionId is required")
    private String sessionId;
    
    @NotNull(message = "eventType is required")
    private EventType eventType;
    
    @NotNull(message = "timestamp is required")
    private Long timestamp;
    
    @NotNull(message = "payload is required")
    private Map<String, Object> payload;
}
