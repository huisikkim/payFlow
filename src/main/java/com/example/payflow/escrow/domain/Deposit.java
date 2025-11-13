package com.example.payflow.escrow.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "escrow_deposits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Deposit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String transactionId;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, length = 50)
    private String depositMethod;  // 계좌이체, 카드 등
    
    @Column(length = 100)
    private String depositReference;  // 입금 참조번호
    
    @Column(nullable = false)
    private LocalDateTime depositedAt;
    
    private LocalDateTime confirmedAt;
    
    public Deposit(String transactionId, BigDecimal amount, String depositMethod) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.depositMethod = depositMethod;
        this.depositedAt = LocalDateTime.now();
    }
    
    public Deposit(String transactionId, BigDecimal amount, String depositMethod, String depositReference) {
        this(transactionId, amount, depositMethod);
        this.depositReference = depositReference;
    }
    
    public void confirm() {
        if (this.confirmedAt != null) {
            throw new IllegalStateException("이미 확인된 입금입니다.");
        }
        this.confirmedAt = LocalDateTime.now();
    }
    
    public boolean isConfirmed() {
        return this.confirmedAt != null;
    }
}
