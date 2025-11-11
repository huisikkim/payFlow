package com.example.payflow.sessionreplay.presentation;

import com.example.payflow.sessionreplay.application.SessionReplayService;
import com.example.payflow.sessionreplay.application.dto.InteractionEventDto;
import com.example.payflow.sessionreplay.application.dto.SessionDto;
import com.example.payflow.sessionreplay.application.dto.SessionFilterDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 세션 재생 REST API Controller
 */
@RestController
@RequestMapping("/api/session-replay")
@RequiredArgsConstructor
@Slf4j
public class SessionReplayController {
    
    private final SessionReplayService sessionReplayService;
    
    /**
     * 세션 목록 조회 (페이징, 필터링)
     */
    @GetMapping("/sessions")
    public ResponseEntity<Page<SessionDto>> getSessions(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @RequestParam(required = false) String userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "startTime") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.debug("Getting sessions: page={}, size={}, userId={}", page, size, userId);
        
        SessionFilterDto filter = SessionFilterDto.builder()
            .startDate(startDate)
            .endDate(endDate)
            .userId(userId)
            .pageSize(size)
            .build();
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") 
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<SessionDto> sessions = sessionReplayService.getSessions(filter, pageable);
        
        return ResponseEntity.ok(sessions);
    }
    
    /**
     * 세션 상세 조회
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<SessionDto> getSession(@PathVariable String sessionId) {
        log.debug("Getting session: {}", sessionId);
        
        SessionDto session = sessionReplayService.getSession(sessionId);
        
        return ResponseEntity.ok(session);
    }
    
    /**
     * 세션 이벤트 조회
     */
    @GetMapping("/sessions/{sessionId}/events")
    public ResponseEntity<List<InteractionEventDto>> getSessionEvents(@PathVariable String sessionId) {
        log.debug("Getting events for session: {}", sessionId);
        
        List<InteractionEventDto> events = sessionReplayService.getSessionEvents(sessionId);
        
        return ResponseEntity.ok(events);
    }
}
