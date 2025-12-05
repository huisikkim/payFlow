package com.example.payflow.manyfast.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollaborationWebSocketHandler extends TextWebSocketHandler {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CopyOnWriteArraySet<WebSocketSession>> projectSessions = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String projectId = getProjectId(session);
        if (projectId != null) {
            projectSessions.computeIfAbsent(projectId, k -> new CopyOnWriteArraySet<>()).add(session);
            log.info("WebSocket connected: {} to project {}", session.getId(), projectId);
        }
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String projectId = getProjectId(session);
        if (projectId == null) return;
        
        // Broadcast to all sessions in the same project except sender
        CopyOnWriteArraySet<WebSocketSession> sessions = projectSessions.get(projectId);
        if (sessions != null) {
            for (WebSocketSession s : sessions) {
                if (s.isOpen() && !s.getId().equals(session.getId())) {
                    s.sendMessage(message);
                }
            }
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String projectId = getProjectId(session);
        if (projectId != null) {
            CopyOnWriteArraySet<WebSocketSession> sessions = projectSessions.get(projectId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    projectSessions.remove(projectId);
                }
            }
            log.info("WebSocket disconnected: {} from project {}", session.getId(), projectId);
        }
    }
    
    private String getProjectId(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.startsWith("projectId=")) {
            return query.substring(10);
        }
        return null;
    }
}
