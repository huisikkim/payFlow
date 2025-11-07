package com.example.payflow.stage.presentation.dto;

import com.example.payflow.stage.domain.StageSettlement;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class StageSettlementResponse {
    private Long id;
    private Long stageId;
    private String stageName;
    private BigDecimal totalPayments;
    private BigDecimal totalPayouts;
    private BigDecimal totalInterest;
    private BigDecimal averageReturn;
    private Boolean isVerified;
    private LocalDateTime settlementDate;
    private List<ParticipantSettlementResponse> participantSettlements;

    public static StageSettlementResponse from(StageSettlement settlement) {
        List<ParticipantSettlementResponse> participants = settlement.getParticipantSettlements()
                .stream()
                .map(ParticipantSettlementResponse::from)
                .collect(Collectors.toList());

        return new StageSettlementResponse(
                settlement.getId(),
                settlement.getStage().getId(),
                settlement.getStage().getName(),
                settlement.getTotalPayments(),
                settlement.getTotalPayouts(),
                settlement.getTotalInterest(),
                settlement.getAverageReturn(),
                settlement.getIsVerified(),
                settlement.getSettlementDate(),
                participants
        );
    }
}
