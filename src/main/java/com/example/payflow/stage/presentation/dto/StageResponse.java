package com.example.payflow.stage.presentation.dto;

import com.example.payflow.stage.domain.Stage;
import com.example.payflow.stage.domain.StageStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StageResponse {
    private Long id;
    private String name;
    private Integer totalParticipants;
    private Integer currentParticipants;
    private BigDecimal monthlyPayment;
    private BigDecimal interestRate;
    private Integer paymentDay;
    private StageStatus status;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private LocalDateTime createdAt;

    public static StageResponse from(Stage stage) {
        return new StageResponse(
                stage.getId(),
                stage.getName(),
                stage.getTotalParticipants(),
                stage.getParticipants().size(),
                stage.getMonthlyPayment(),
                stage.getInterestRate(),
                stage.getPaymentDay(),
                stage.getStatus(),
                stage.getStartDate(),
                stage.getExpectedEndDate(),
                stage.getCreatedAt()
        );
    }
}
