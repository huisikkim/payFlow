package com.example.payflow.chatbot.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private Long conversationId;
    private String message;
    private String intent;
    private String role;
    private LocalDateTime timestamp;

    public ChatResponse(Long conversationId, String message, String intent) {
        this.conversationId = conversationId;
        this.message = message;
        this.intent = intent;
        this.timestamp = LocalDateTime.now();
    }
}
