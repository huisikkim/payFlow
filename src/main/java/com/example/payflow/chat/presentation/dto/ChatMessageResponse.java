package com.example.payflow.chat.presentation.dto;

import com.example.payflow.chat.domain.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private String roomId;
    private String senderId;
    private ChatMessage.SenderType senderType;
    private ChatMessage.MessageType messageType;
    private String content;
    private String metadata;
    private Boolean isRead;
    private LocalDateTime createdAt;
    
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getRoomId(),
                message.getSenderId(),
                message.getSenderType(),
                message.getMessageType(),
                message.getContent(),
                message.getMetadata(),
                message.getIsRead(),
                message.getCreatedAt()
        );
    }
}
