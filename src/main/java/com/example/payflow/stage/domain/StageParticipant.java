package com.example.payflow.stage.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stage_participants", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"stage_id", "turn_number"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StageParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @Column(nullable = false)
    private String username; // 참여자 사용자명

    @Column(nullable = false)
    private Integer turnNumber; // 선택한 순번 (1부터 시작)

    @Column(nullable = false)
    private Boolean hasReceivedPayout = false; // 약정금 수령 여부

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    private LocalDateTime payoutReceivedAt; // 약정금 수령 시각

    public StageParticipant(Stage stage, String username, Integer turnNumber) {
        validateTurnNumber(stage, turnNumber);
        
        this.stage = stage;
        this.username = username;
        this.turnNumber = turnNumber;
        this.joinedAt = LocalDateTime.now();
    }

    private void validateTurnNumber(Stage stage, Integer turnNumber) {
        if (turnNumber < 1 || turnNumber > stage.getTotalParticipants()) {
            throw new IllegalArgumentException(
                String.format("순번은 1부터 %d 사이여야 합니다.", stage.getTotalParticipants())
            );
        }
    }

    public void markPayoutReceived() {
        if (this.hasReceivedPayout) {
            throw new IllegalStateException("이미 약정금을 수령했습니다.");
        }
        this.hasReceivedPayout = true;
        this.payoutReceivedAt = LocalDateTime.now();
    }

    public boolean isPayoutDue(Integer currentMonth) {
        return this.turnNumber.equals(currentMonth) && !this.hasReceivedPayout;
    }
}
