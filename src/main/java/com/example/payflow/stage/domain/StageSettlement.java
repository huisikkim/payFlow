package com.example.payflow.stage.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stage_settlements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StageSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false, unique = true)
    private Stage stage;

    @Column(nullable = false)
    private BigDecimal totalPayments; // 총 납입액

    @Column(nullable = false)
    private BigDecimal totalPayouts; // 총 지급액

    @Column(nullable = false)
    private BigDecimal totalInterest; // 총 이자

    @Column(nullable = false)
    private LocalDateTime settlementDate; // 정산일

    @Column(nullable = false)
    private Boolean isVerified = false; // 검증 완료 여부

    @OneToMany(mappedBy = "settlement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParticipantSettlement> participantSettlements = new ArrayList<>();

    public StageSettlement(Stage stage, BigDecimal totalPayments, BigDecimal totalPayouts) {
        this.stage = stage;
        this.totalPayments = totalPayments;
        this.totalPayouts = totalPayouts;
        this.totalInterest = totalPayouts.subtract(totalPayments);
        this.settlementDate = LocalDateTime.now();
    }

    public void addParticipantSettlement(ParticipantSettlement participantSettlement) {
        this.participantSettlements.add(participantSettlement);
    }

    public void verify() {
        // 총 납입액과 총 지급액이 일치하는지 검증
        BigDecimal calculatedPayouts = participantSettlements.stream()
                .map(ParticipantSettlement::getTotalReceived)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal calculatedPayments = participantSettlements.stream()
                .map(ParticipantSettlement::getTotalPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (calculatedPayments.compareTo(totalPayments) != 0) {
            throw new IllegalStateException(
                String.format("납입액 불일치: 예상=%s, 실제=%s", totalPayments, calculatedPayments)
            );
        }

        if (calculatedPayouts.compareTo(totalPayouts) != 0) {
            throw new IllegalStateException(
                String.format("지급액 불일치: 예상=%s, 실제=%s", totalPayouts, calculatedPayouts)
            );
        }

        this.isVerified = true;
    }

    public BigDecimal getAverageReturn() {
        if (totalPayments.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalInterest.divide(totalPayments, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
