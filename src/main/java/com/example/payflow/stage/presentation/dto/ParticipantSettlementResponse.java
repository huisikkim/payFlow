package com.example.payflow.stage.presentation.dto;

import com.example.payflow.stage.domain.ParticipantSettlement;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ParticipantSettlementResponse {
    private Long id;
    private String username;
    private Integer turnNumber;
    private BigDecimal totalPaid;
    private BigDecimal totalReceived;
    private BigDecimal profitLoss;
    private BigDecimal effectiveRate;
    private Integer paidMonths;
    private Integer receivedMonth;
    private Boolean isProfitable;

    public static ParticipantSettlementResponse from(ParticipantSettlement settlement) {
        return new ParticipantSettlementResponse(
                settlement.getId(),
                settlement.getUsername(),
                settlement.getTurnNumber(),
                settlement.getTotalPaid(),
                settlement.getTotalReceived(),
                settlement.getProfitLoss(),
                settlement.getEffectiveRate(),
                settlement.getPaidMonths(),
                settlement.getReceivedMonth(),
                settlement.isProfitable()
        );
    }
}
