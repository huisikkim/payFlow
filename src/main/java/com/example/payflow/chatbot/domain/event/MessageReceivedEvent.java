package com.example.payflow.chatbot.domain.event;

import com.example.payflow.chatbot.domain.Intent;
import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class MessageReceivedEvent implements DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredOn;
    private final Long conversationId;
    private final String message;
    private final Intent detectedIntent;

    public MessageReceivedEvent(Long conversationId, String message, Intent detectedIntent) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.conversationId = conversationId;
        this.message = message;
        this.detectedIntent = detectedIntent;
    }

    @Override
    public String getEventType() {
        return "chatbot.message.received";
    }
}
