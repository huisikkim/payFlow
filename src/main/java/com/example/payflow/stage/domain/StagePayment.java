package com.example.payflow.stage.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stage_payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StagePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @Column(nullable = false)
    private String username; // 납입자

    @Column(nullable = false)
    private Integer monthNumber; // 몇 번째 달 납입인지 (1부터 시작)

    @Column(nullable = false)
    private BigDecimal amount; // 납입액

    @Column(nullable = false)
    private Boolean isPaid = false; // 납입 완료 여부

    @Column(nullable = false)
    private LocalDateTime dueDate; // 납입 기한

    private LocalDateTime paidAt; // 실제 납입 시각

    private String paymentKey; // 토스페이먼츠 결제 키

    public StagePayment(Stage stage, String username, Integer monthNumber, 
                       BigDecimal amount, LocalDateTime dueDate) {
        this.stage = stage;
        this.username = username;
        this.monthNumber = monthNumber;
        this.amount = amount;
        this.dueDate = dueDate;
    }

    public void markAsPaid(String paymentKey) {
        if (this.isPaid) {
            throw new IllegalStateException("이미 납입 완료된 결제입니다.");
        }
        this.isPaid = true;
        this.paidAt = LocalDateTime.now();
        this.paymentKey = paymentKey;
    }

    public boolean isOverdue() {
        return !this.isPaid && LocalDateTime.now().isAfter(this.dueDate);
    }
}
