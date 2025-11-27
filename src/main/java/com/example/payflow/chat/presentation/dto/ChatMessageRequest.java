package com.example.payflow.chat.presentation.dto;

import com.example.payflow.chat.domain.ChatMessage;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageRequest {
    private String content;
    private ChatMessage.MessageType messageType = ChatMessage.MessageType.TEXT;
    private String metadata;  // JSON 형태의 추가 정보
}
