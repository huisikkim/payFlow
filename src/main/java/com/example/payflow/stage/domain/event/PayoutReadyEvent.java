package com.example.payflow.stage.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class PayoutReadyEvent implements DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final Long stageId;
    private final String username;
    private final Integer turnNumber;
    private final BigDecimal amount;

    public PayoutReadyEvent(Long stageId, String username, Integer turnNumber, BigDecimal amount) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.stageId = stageId;
        this.username = username;
        this.turnNumber = turnNumber;
        this.amount = amount;
    }

    @Override
    public String getEventType() {
        return "PayoutReady";
    }
}
