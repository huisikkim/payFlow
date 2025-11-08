package com.example.payflow.chatbot.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class EscalationRequiredEvent implements DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredOn;
    private final Long conversationId;
    private final String reason;

    public EscalationRequiredEvent(Long conversationId, String reason) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.conversationId = conversationId;
        this.reason = reason;
    }

    @Override
    public String getEventType() {
        return "chatbot.escalation.required";
    }
}
