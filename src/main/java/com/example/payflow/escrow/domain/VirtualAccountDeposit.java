package com.example.payflow.escrow.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 가상계좌 입금 엔티티
 */
@Entity
@Table(name = "escrow_virtual_account_deposits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VirtualAccountDeposit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String transactionId;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, length = 100)
    private String virtualAccountNumber;  // 가상계좌 번호
    
    @Column(nullable = false, length = 50)
    private String bankCode;  // 은행 코드
    
    @Column(nullable = false, length = 100)
    private String bankName;  // 은행명
    
    @Column(nullable = false)
    private LocalDateTime dueDate;  // 입금 기한
    
    @Column(length = 100)
    private String depositorName;  // 입금자명
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VirtualAccountStatus status;  // 상태
    
    @Column(length = 100)
    private String paymentKey;  // 토스 결제 키
    
    @Column(length = 100)
    private String orderId;  // 주문 ID
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime depositedAt;  // 실제 입금 시각
    
    private LocalDateTime canceledAt;  // 취소 시각
    
    @Column(length = 500)
    private String cancelReason;  // 취소 사유
    
    public VirtualAccountDeposit(
            String transactionId,
            BigDecimal amount,
            String virtualAccountNumber,
            String bankCode,
            String bankName,
            LocalDateTime dueDate,
            String paymentKey,
            String orderId) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.virtualAccountNumber = virtualAccountNumber;
        this.bankCode = bankCode;
        this.bankName = bankName;
        this.dueDate = dueDate;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.status = VirtualAccountStatus.WAITING_FOR_DEPOSIT;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * 입금 완료 처리
     */
    public void completeDeposit(String depositorName) {
        if (this.status != VirtualAccountStatus.WAITING_FOR_DEPOSIT) {
            throw new IllegalStateException("입금 대기 상태가 아닙니다: " + this.status);
        }
        this.status = VirtualAccountStatus.DONE;
        this.depositorName = depositorName;
        this.depositedAt = LocalDateTime.now();
    }
    
    /**
     * 입금 취소 처리
     */
    public void cancel(String reason) {
        if (this.status == VirtualAccountStatus.DONE) {
            throw new IllegalStateException("이미 입금 완료된 가상계좌는 취소할 수 없습니다");
        }
        this.status = VirtualAccountStatus.CANCELED;
        this.cancelReason = reason;
        this.canceledAt = LocalDateTime.now();
    }
    
    /**
     * 입금 기한 만료 처리
     */
    public void expire() {
        if (this.status == VirtualAccountStatus.WAITING_FOR_DEPOSIT) {
            this.status = VirtualAccountStatus.EXPIRED;
            this.canceledAt = LocalDateTime.now();
            this.cancelReason = "입금 기한 만료";
        }
    }
    
    public boolean isWaitingForDeposit() {
        return this.status == VirtualAccountStatus.WAITING_FOR_DEPOSIT;
    }
    
    public boolean isCompleted() {
        return this.status == VirtualAccountStatus.DONE;
    }
}
