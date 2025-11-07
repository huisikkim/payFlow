package com.example.payflow.stage.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class StageCompletedEvent implements DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final Long stageId;
    private final String stageName;
    private final LocalDate completedDate;

    public StageCompletedEvent(Long stageId, String stageName, LocalDate completedDate) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.stageId = stageId;
        this.stageName = stageName;
        this.completedDate = completedDate;
    }

    @Override
    public String getEventType() {
        return "StageCompleted";
    }
}
