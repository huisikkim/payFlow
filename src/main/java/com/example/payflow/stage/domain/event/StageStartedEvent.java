package com.example.payflow.stage.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class StageStartedEvent implements DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final Long stageId;
    private final String stageName;
    private final Integer totalParticipants;
    private final LocalDate startDate;
    private final Integer paymentDay;

    public StageStartedEvent(Long stageId, String stageName, Integer totalParticipants, 
                            LocalDate startDate, Integer paymentDay) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.stageId = stageId;
        this.stageName = stageName;
        this.totalParticipants = totalParticipants;
        this.startDate = startDate;
        this.paymentDay = paymentDay;
    }

    @Override
    public String getEventType() {
        return "StageStarted";
    }
}
