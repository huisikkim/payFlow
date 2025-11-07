package com.example.payflow.stage.presentation.dto;

import com.example.payflow.stage.domain.StagePayout;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StagePayoutResponse {
    private Long id;
    private Long stageId;
    private String stageName;
    private String username;
    private Integer turnNumber;
    private BigDecimal amount;
    private Boolean isCompleted;
    private LocalDateTime scheduledAt;
    private LocalDateTime completedAt;
    private String transactionId;

    public static StagePayoutResponse from(StagePayout payout) {
        return new StagePayoutResponse(
                payout.getId(),
                payout.getStage().getId(),
                payout.getStage().getName(),
                payout.getUsername(),
                payout.getTurnNumber(),
                payout.getAmount(),
                payout.getIsCompleted(),
                payout.getScheduledAt(),
                payout.getCompletedAt(),
                payout.getTransactionId()
        );
    }
}
