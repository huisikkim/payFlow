package com.example.payflow.stage.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stage_payouts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StagePayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @Column(nullable = false)
    private String username; // 수령자

    @Column(nullable = false)
    private Integer turnNumber; // 순번

    @Column(nullable = false)
    private BigDecimal amount; // 약정금

    @Column(nullable = false)
    private Boolean isCompleted = false; // 지급 완료 여부

    @Column(nullable = false)
    private LocalDateTime scheduledAt; // 지급 예정일

    private LocalDateTime completedAt; // 실제 지급 시각

    private String transactionId; // 거래 ID

    public StagePayout(Stage stage, String username, Integer turnNumber, 
                      BigDecimal amount, LocalDateTime scheduledAt) {
        this.stage = stage;
        this.username = username;
        this.turnNumber = turnNumber;
        this.amount = amount;
        this.scheduledAt = scheduledAt;
    }

    public void complete(String transactionId) {
        if (this.isCompleted) {
            throw new IllegalStateException("이미 지급 완료된 약정금입니다.");
        }
        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();
        this.transactionId = transactionId;
    }
}
