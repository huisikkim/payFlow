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
            String senderId = principal.getName();
            
            // 사용자 타입 추출
            ChatMessage.SenderType senderType = extractSenderType(headerAccessor);
            
            // 메시지 저장
            ChatMessage message = chatService.sendMessage(
                    roomId,
                    senderId,
                    senderType,
                    request.getMessageType(),
                    request.getContent(),
                    request.getMetadata()
            );
            
            // 채팅방 구독자들에게 브로드캐스트
            ChatMessageResponse response = ChatMessageResponse.from(message);
            messagingTemplate.convertAndSend("/topic/chat/" + roomId, response);
            
            log.info("WebSocket 메시지 전송 완료 - roomId: {}, senderId: {}", roomId, senderId);
            
        } catch (Exception e) {
            log.error("메시지 전송 실패 - roomId: {}, error: {}", roomId, e.getMessage(), e);
        }
    }
    
    private ChatMessage.SenderType extractSenderType(SimpMessageHeaderAccessor headerAccessor) {
        // Spring Security의 권한 정보에서 사용자 타입 추출
        Object authorities = headerAccessor.getSessionAttributes().get("authorities");
        if (authorities != null && authorities.toString().contains("ROLE_STORE_OWNER")) {
            return ChatMessage.SenderType.STORE;
        }
        return ChatMessage.SenderType.DISTRIBUTOR;
    }
}
