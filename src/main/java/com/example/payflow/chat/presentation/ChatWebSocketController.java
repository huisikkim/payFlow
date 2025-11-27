package com.example.payflow.chat.presentation;

import com.example.payflow.chat.application.ChatService;
import com.example.payflow.chat.domain.ChatMessage;
import com.example.payflow.chat.presentation.dto.ChatMessageRequest;
import com.example.payflow.chat.presentation.dto.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {
    
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * 메시지 전송
     * 클라이언트: /app/chat/{roomId}
     * 구독: /topic/chat/{roomId}
     */
    @MessageMapping("/chat/{roomId}")
    public void sendMessage(
            @DestinationVariable String roomId,
            @Payload ChatMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {
        
        try {
            // Principal에서 인증된 사용자 정보 추출 (클라이언트가 보낸 정보 무시)
            if (principal == null) {
                log.error("인증되지 않은 사용자의 메시지 전송 시도 - roomId: {}", roomId);
                return;
            }
            
            String senderId = principal.getName();
            log.info("WebSocket 메시지 수신 - roomId: {}, 인증된 senderId: {}", roomId, senderId);
            
            // 사용자 타입 추출 (세션에 저장된 권한 정보 사용)
            ChatMessage.SenderType senderType = extractSenderType(headerAccessor);
            
            // 메시지 저장 (서버에서 인증한 senderId 사용)
            ChatMessage message = chatService.sendMessage(
                    roomId,
                    senderId,  // JWT 토큰에서 추출한 username 사용
                    senderType,
                    request.getMessageType(),
                    request.getContent(),
                    request.getMetadata()
            );
            
            // 채팅방 구독자들에게 브로드캐스트
            ChatMessageResponse response = ChatMessageResponse.from(message);
            messagingTemplate.convertAndSend("/topic/chat/" + roomId, response);
            
            log.info("WebSocket 메시지 전송 완료 - roomId: {}, senderId: {}, messageType: {}", 
                    roomId, senderId, request.getMessageType());
            
        } catch (Exception e) {
            log.error("메시지 전송 실패 - roomId: {}, error: {}", roomId, e.getMessage(), e);
            // 클라이언트에게 에러 전송 (선택사항)
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    "메시지 전송 실패: " + e.getMessage()
            );
        }
    }
    
    private ChatMessage.SenderType extractSenderType(SimpMessageHeaderAccessor headerAccessor) {
        // Spring Security의 권한 정보에서 사용자 타입 추출
        Object authorities = headerAccessor.getSessionAttributes().get("authorities");
        
        if (authorities != null) {
            String authStr = authorities.toString();
            log.debug("사용자 권한: {}", authStr);
            
            if (authStr.contains("ROLE_STORE_OWNER")) {
                return ChatMessage.SenderType.STORE;
            } else if (authStr.contains("ROLE_DISTRIBUTOR")) {
                return ChatMessage.SenderType.DISTRIBUTOR;
            }
        }
        
        log.warn("권한 정보를 찾을 수 없음, 기본값 DISTRIBUTOR 사용");
        return ChatMessage.SenderType.DISTRIBUTOR;
    }
}
