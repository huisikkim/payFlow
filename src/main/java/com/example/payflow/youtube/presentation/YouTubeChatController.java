package com.example.payflow.youtube.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class YouTubeChatController {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * YouTube 익명 채팅 메시지 전송
     * 클라이언트: /app/youtube/chat
     * 구독: /topic/youtube/popular
     */
    @MessageMapping("/youtube/chat")
    public void sendMessage(
            @Payload Map<String, String> message,
            SimpMessageHeaderAccessor headerAccessor) {
        
        try {
            // 세션에서 익명 사용자 정보 추출
            String anonymousUser = (String) headerAccessor.getSessionAttributes().get("anonymousUser");
            Boolean isAnonymous = (Boolean) headerAccessor.getSessionAttributes().get("isAnonymous");
            
            if (isAnonymous == null || !isAnonymous || anonymousUser == null) {
                log.error("익명 사용자 정보 없음");
                return;
            }
            
            String content = message.get("content");
            if (content == null || content.trim().isEmpty()) {
                log.warn("빈 메시지 전송 시도 - user: {}", anonymousUser);
                return;
            }
            
            // 메시지 길이 제한 (500자)
            if (content.length() > 500) {
                content = content.substring(0, 500);
            }
            
            // 응답 메시지 생성
            Map<String, Object> response = Map.of(
                "username", anonymousUser,
                "content", content,
                "timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                "type", "message"
            );
            
            // 모든 구독자에게 브로드캐스트
            messagingTemplate.convertAndSend("/topic/youtube/popular", response);
            
            log.info("YouTube 채팅 메시지 전송 - user: {}, content: {}", anonymousUser, content);
            
        } catch (Exception e) {
            log.error("YouTube 채팅 메시지 전송 실패: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 사용자 입장 알림
     */
    @MessageMapping("/youtube/join")
    public void userJoin(SimpMessageHeaderAccessor headerAccessor) {
        try {
            String anonymousUser = (String) headerAccessor.getSessionAttributes().get("anonymousUser");
            
            if (anonymousUser != null) {
                Map<String, Object> response = Map.of(
                    "username", anonymousUser,
                    "content", anonymousUser + "님이 입장했습니다.",
                    "timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    "type", "join"
                );
                
                messagingTemplate.convertAndSend("/topic/youtube/popular", response);
                log.info("YouTube 채팅 입장 - user: {}", anonymousUser);
            }
        } catch (Exception e) {
            log.error("사용자 입장 알림 실패: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 타이핑 인디케이터
     */
    @MessageMapping("/youtube/typing")
    public void userTyping(
            @Payload Map<String, Object> data,
            SimpMessageHeaderAccessor headerAccessor) {
        try {
            String anonymousUser = (String) headerAccessor.getSessionAttributes().get("anonymousUser");
            Boolean isAnonymous = (Boolean) headerAccessor.getSessionAttributes().get("isAnonymous");
            
            if (isAnonymous == null || !isAnonymous || anonymousUser == null) {
                return;
            }
            
            Boolean isTyping = (Boolean) data.get("isTyping");
            if (isTyping == null) {
                return;
            }
            
            Map<String, Object> response = Map.of(
                "username", anonymousUser,
                "isTyping", isTyping,
                "type", "typing"
            );
            
            messagingTemplate.convertAndSend("/topic/youtube/popular/typing", response);
            log.debug("타이핑 인디케이터 - user: {}, isTyping: {}", anonymousUser, isTyping);
            
        } catch (Exception e) {
            log.error("타이핑 인디케이터 전송 실패: {}", e.getMessage(), e);
        }
    }
}
