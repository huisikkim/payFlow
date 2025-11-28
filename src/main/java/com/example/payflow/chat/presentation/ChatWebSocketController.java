package com.example.payflow.chat.presentation;

import com.example.payflow.chat.application.ChatService;
import com.example.payflow.chat.domain.ChatMessage;
import com.example.payflow.chat.domain.ChatRoom;
import com.example.payflow.chat.presentation.dto.ChatMessageRequest;
import com.example.payflow.chat.presentation.dto.ChatMessageResponse;
import com.example.payflow.chat.presentation.dto.TypingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

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
    
    /**
     * 타이핑 인디케이터 처리
     * 클라이언트: /app/chat/{roomId}/typing
     * 구독: /topic/chat/{roomId}/typing
     */
    @MessageMapping("/chat/{roomId}/typing")
    public void handleTyping(
            @DestinationVariable String roomId,
            @Payload TypingEvent event,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {
        
        try {
            // 인증 확인
            if (principal == null) {
                log.error("인증되지 않은 사용자의 타이핑 이벤트 - roomId: {}", roomId);
                return;
            }
            
            String userId = principal.getName();
            log.debug("타이핑 이벤트 수신 - roomId: {}, userId: {}, isTyping: {}", 
                    roomId, userId, event.isTyping());
            
            // 채팅방 접근 권한 검증
            ChatRoom chatRoom = chatService.getChatRoom(roomId);
            ChatMessage.SenderType senderType = extractSenderType(headerAccessor);
            
            boolean hasAccess = (senderType == ChatMessage.SenderType.STORE && 
                                chatRoom.getStoreId().equals(userId)) ||
                               (senderType == ChatMessage.SenderType.DISTRIBUTOR && 
                                chatRoom.getDistributorId().equals(userId));
            
            if (!hasAccess) {
                log.warn("채팅방 접근 권한 없음 - roomId: {}, userId: {}", roomId, userId);
                return;
            }
            
            // 서버에서 사용자 정보 설정 (클라이언트가 보낸 정보 무시)
            event.setUserId(userId);
            event.setTimestamp(LocalDateTime.now());
            
            // 사용자 이름 설정 (선택사항)
            if (senderType == ChatMessage.SenderType.STORE) {
                event.setUserName(chatRoom.getStoreName());
            } else {
                event.setUserName(chatRoom.getDistributorName());
            }
            
            // 같은 채팅방의 다른 사용자들에게 브로드캐스트
            messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/typing", event);
            
            log.debug("타이핑 이벤트 브로드캐스트 완료 - roomId: {}, userId: {}, isTyping: {}", 
                    roomId, userId, event.isTyping());
            
        } catch (IllegalArgumentException e) {
            log.error("타이핑 이벤트 처리 실패 (채팅방 없음) - roomId: {}, error: {}", roomId, e.getMessage());
        } catch (Exception e) {
            log.error("타이핑 이벤트 처리 실패 - roomId: {}, error: {}", roomId, e.getMessage(), e);
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
