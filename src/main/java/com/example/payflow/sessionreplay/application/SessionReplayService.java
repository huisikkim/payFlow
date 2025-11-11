package com.example.payflow.sessionreplay.application;

import com.example.payflow.sessionreplay.application.dto.InteractionEventDto;
import com.example.payflow.sessionreplay.application.dto.SessionDto;
import com.example.payflow.sessionreplay.application.dto.SessionFilterDto;
import com.example.payflow.sessionreplay.domain.InteractionEvent;
import com.example.payflow.sessionreplay.domain.InteractionEventRepository;
import com.example.payflow.sessionreplay.domain.Session;
import com.example.payflow.sessionreplay.domain.SessionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 세션 재생 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionReplayService {
    
    private final SessionRepository sessionRepository;
    private final InteractionEventRepository interactionEventRepository;
    private final ObjectMapper objectMapper;
    private final com.example.payflow.sessionreplay.infrastructure.PayloadEncryptor payloadEncryptor;
    
    /**
     * 세션 목록 조회 (페이징, 필터링)
     */
    @Transactional(readOnly = true)
    public Page<SessionDto> getSessions(SessionFilterDto filter, Pageable pageable) {
        Page<Session> sessions;
        
        if (filter.getUserId() != null && filter.getStartDate() != null && filter.getEndDate() != null) {
            // 사용자 ID와 날짜 범위로 필터링
            sessions = sessionRepository.findByUserId(filter.getUserId(), pageable);
            // 추가 날짜 필터링은 애플리케이션 레벨에서 처리 가능
        } else if (filter.getUserId() != null) {
            sessions = sessionRepository.findByUserId(filter.getUserId(), pageable);
        } else if (filter.getStartDate() != null && filter.getEndDate() != null) {
            sessions = sessionRepository.findByStartTimeBetween(
                filter.getStartDate(), filter.getEndDate(), pageable);
        } else {
            sessions = sessionRepository.findAll(pageable);
        }
        
        return sessions.map(this::toDto);
    }
    
    /**
     * 세션 상세 조회
     */
    @Transactional(readOnly = true)
    public SessionDto getSession(String sessionId) {
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));
        
        return toDto(session);
    }
    
    /**
     * 세션 이벤트 조회
     */
    @Transactional(readOnly = true)
    public List<InteractionEventDto> getSessionEvents(String sessionId) {
        // 세션 존재 확인
        if (!sessionRepository.findById(sessionId).isPresent()) {
            throw new SessionNotFoundException("Session not found: " + sessionId);
        }
        
        List<InteractionEvent> events = interactionEventRepository
            .findBySessionIdOrderByTimestamp(sessionId);
        
        return events.stream()
            .map(this::toEventDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Session Entity를 DTO로 변환
     */
    private SessionDto toDto(Session session) {
        return SessionDto.builder()
            .sessionId(session.getSessionId())
            .userId(session.getUserId())
            .startTime(session.getStartTime())
            .endTime(session.getEndTime())
            .totalEvents(session.getTotalEvents())
            .deviceInfo(session.getDeviceInfo())
            .durationSeconds(session.getDuration().getSeconds())
            .build();
    }
    
    /**
     * InteractionEvent Entity를 DTO로 변환
     */
    private InteractionEventDto toEventDto(InteractionEvent event) {
        Map<String, Object> payload = deserializePayload(event.getPayload());
        
        return InteractionEventDto.builder()
            .sessionId(event.getSessionId())
            .eventType(event.getEventType())
            .timestamp(event.getTimestamp())
            .payload(payload)
            .build();
    }
    
    /**
     * JSON 문자열을 Map으로 역직렬화 (복호화 포함)
     */
    private Map<String, Object> deserializePayload(String encryptedPayload) {
        try {
            String payloadJson = payloadEncryptor.decrypt(encryptedPayload);
            return objectMapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize payload", e);
            return Map.of();
        } catch (Exception e) {
            log.error("Failed to decrypt payload", e);
            return Map.of();
        }
    }
}
