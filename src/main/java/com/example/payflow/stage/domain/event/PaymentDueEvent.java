package com.example.payflow.stage.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class PaymentDueEvent implements DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final Long stageId;
    private final String username;
    private final Integer monthNumber;
    private final BigDecimal amount;
    private final LocalDateTime dueDate;

    public PaymentDueEvent(Long stageId, String username, Integer monthNumber, 
                          BigDecimal amount, LocalDateTime dueDate) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.stageId = stageId;
        this.username = username;
        this.monthNumber = monthNumber;
        this.amount = amount;
        this.dueDate = dueDate;
    }

    @Override
    public String getEventType() {
        return "PaymentDue";
    }
}
