package com.example.payflow.stage.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer totalParticipants; // 총 참여 인원

    @Column(nullable = false)
    private BigDecimal monthlyPayment; // 월 납입액

    @Column(nullable = false)
    private BigDecimal interestRate; // 이율 (예: 0.05 = 5%)

    @Column(nullable = false)
    private Integer paymentDay; // 매월 결제일 (1-28)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StageStatus status;

    private LocalDate startDate; // 스테이지 시작일

    private LocalDate expectedEndDate; // 예상 종료일

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StageParticipant> participants = new ArrayList<>();

    public Stage(String name, Integer totalParticipants, BigDecimal monthlyPayment, 
                 BigDecimal interestRate, Integer paymentDay) {
        validateInputs(totalParticipants, monthlyPayment, interestRate, paymentDay);
        
        this.name = name;
        this.totalParticipants = totalParticipants;
        this.monthlyPayment = monthlyPayment;
        this.interestRate = interestRate;
        this.paymentDay = paymentDay;
        this.status = StageStatus.RECRUITING;
        this.createdAt = LocalDateTime.now();
    }

    private void validateInputs(Integer totalParticipants, BigDecimal monthlyPayment, 
                                BigDecimal interestRate, Integer paymentDay) {
        if (totalParticipants < 2) {
            throw new IllegalArgumentException("참여 인원은 최소 2명 이상이어야 합니다.");
        }
        if (monthlyPayment.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("월 납입액은 0보다 커야 합니다.");
        }
        if (interestRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("이율은 0 이상이어야 합니다.");
        }
        if (paymentDay < 1 || paymentDay > 28) {
            throw new IllegalArgumentException("결제일은 1일에서 28일 사이여야 합니다.");
        }
    }

    public void addParticipant(StageParticipant participant) {
        if (this.status != StageStatus.RECRUITING) {
            throw new IllegalStateException("모집 중인 스테이지만 참여할 수 있습니다.");
        }
        if (this.participants.size() >= this.totalParticipants) {
            throw new IllegalStateException("참여 인원이 마감되었습니다.");
        }
        this.participants.add(participant);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isFullyRecruited() {
        return this.participants.size() == this.totalParticipants;
    }

    public void start() {
        if (this.status != StageStatus.RECRUITING) {
            throw new IllegalStateException("모집 중인 스테이지만 시작할 수 있습니다.");
        }
        if (!isFullyRecruited()) {
            throw new IllegalStateException("모든 참여자가 모집되어야 시작할 수 있습니다.");
        }
        
        this.status = StageStatus.ACTIVE;
        this.startDate = LocalDate.now();
        this.expectedEndDate = this.startDate.plusMonths(this.totalParticipants);
        this.updatedAt = LocalDateTime.now();
    }

    public void complete() {
        if (this.status != StageStatus.ACTIVE) {
            throw new IllegalStateException("진행 중인 스테이지만 종료할 수 있습니다.");
        }
        this.status = StageStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status == StageStatus.COMPLETED) {
            throw new IllegalStateException("완료된 스테이지는 취소할 수 없습니다.");
        }
        this.status = StageStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal calculatePayoutAmount(Integer turnNumber) {
        // 약정금 = 월 납입액 * 총 인원 * (1 + 이율 * (순번 - 1))
        BigDecimal baseAmount = monthlyPayment.multiply(BigDecimal.valueOf(totalParticipants));
        BigDecimal interestMultiplier = interestRate.multiply(BigDecimal.valueOf(turnNumber - 1));
        BigDecimal totalMultiplier = BigDecimal.ONE.add(interestMultiplier);
        return baseAmount.multiply(totalMultiplier);
    }
}
