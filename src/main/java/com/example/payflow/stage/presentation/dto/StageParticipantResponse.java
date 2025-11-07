package com.example.payflow.stage.presentation.dto;

import com.example.payflow.stage.domain.StageParticipant;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StageParticipantResponse {
    private Long id;
    private String username;
    private Integer turnNumber;
    private Boolean hasReceivedPayout;
    private LocalDateTime joinedAt;
    private LocalDateTime payoutReceivedAt;

    public static StageParticipantResponse from(StageParticipant participant) {
        return new StageParticipantResponse(
                participant.getId(),
                participant.getUsername(),
                participant.getTurnNumber(),
                participant.getHasReceivedPayout(),
                participant.getJoinedAt(),
                participant.getPayoutReceivedAt()
        );
    }
}
