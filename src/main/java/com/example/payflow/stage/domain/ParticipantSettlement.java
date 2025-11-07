package com.example.payflow.stage.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "participant_settlements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParticipantSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false)
    private StageSettlement settlement;

    @Column(nullable = false)
    private String username; // 참여자

    @Column(nullable = false)
    private Integer turnNumber; // 순번

    @Column(nullable = false)
    private BigDecimal totalPaid; // 총 납입액

    @Column(nullable = false)
    private BigDecimal totalReceived; // 총 수령액

    @Column(nullable = false)
    private BigDecimal profitLoss; // 손익

    @Column(nullable = false)
    private BigDecimal effectiveRate; // 실질 이율 (%)

    @Column(nullable = false)
    private Integer paidMonths; // 납입 개월 수

    @Column(nullable = false)
    private Integer receivedMonth; // 수령 월차

    public ParticipantSettlement(StageSettlement settlement, String username, Integer turnNumber,
                                BigDecimal totalPaid, BigDecimal totalReceived, 
                                Integer paidMonths, Integer receivedMonth) {
        this.settlement = settlement;
        this.username = username;
        this.turnNumber = turnNumber;
        this.totalPaid = totalPaid;
        this.totalReceived = totalReceived;
        this.paidMonths = paidMonths;
        this.receivedMonth = receivedMonth;
        
        // 손익 계산
        this.profitLoss = totalReceived.subtract(totalPaid);
        
        // 실질 이율 계산 (손익 / 납입액 × 100)
        if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            this.effectiveRate = profitLoss
                    .divide(totalPaid, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.effectiveRate = BigDecimal.ZERO;
        }
    }

    public boolean isProfitable() {
        return profitLoss.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isBreakEven() {
        return profitLoss.compareTo(BigDecimal.ZERO) == 0;
    }
}
