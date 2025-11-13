package com.example.payflow.escrow.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "escrow_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EscrowTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String transactionId;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "userId", column = @Column(name = "buyer_user_id")),
        @AttributeOverride(name = "name", column = @Column(name = "buyer_name")),
        @AttributeOverride(name = "email", column = @Column(name = "buyer_email")),
        @AttributeOverride(name = "phone", column = @Column(name = "buyer_phone"))
    })
    private Participant buyer;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "userId", column = @Column(name = "seller_user_id")),
        @AttributeOverride(name = "name", column = @Column(name = "seller_name")),
        @AttributeOverride(name = "email", column = @Column(name = "seller_email")),
        @AttributeOverride(name = "phone", column = @Column(name = "seller_phone"))
    })
    private Participant seller;
    
    @Embedded
    private Vehicle vehicle;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal feeRate;  // 수수료율 (예: 0.03 = 3%)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EscrowStatus status;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime completedAt;
    
    public EscrowTransaction(Participant buyer, Participant seller, Vehicle vehicle, BigDecimal amount) {
        this(buyer, seller, vehicle, amount, new BigDecimal("0.03")); // 기본 수수료율 3%
    }
    
    public EscrowTransaction(Participant buyer, Participant seller, Vehicle vehicle, BigDecimal amount, BigDecimal feeRate) {
        this.transactionId = UUID.randomUUID().toString();
        this.buyer = buyer;
        this.seller = seller;
        this.vehicle = vehicle;
        this.amount = amount;
        this.feeRate = feeRate;
        this.status = EscrowStatus.INITIATED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // 비즈니스 메서드
    
    public void confirmDeposit(BigDecimal depositAmount) {
        validateState(EscrowStatus.INITIATED, "입금 확인은 INITIATED 상태에서만 가능합니다.");
        
        if (depositAmount.compareTo(this.amount) != 0) {
            throw new DepositAmountMismatchException(this.amount, depositAmount);
        }
        
        this.status = EscrowStatus.DEPOSITED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void confirmDelivery() {
        validateState(EscrowStatus.DEPOSITED, "차량 인도 확인은 DEPOSITED 상태에서만 가능합니다.");
        
        this.status = EscrowStatus.DELIVERED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void verifyVehicle(VerificationResult result) {
        validateState(EscrowStatus.DELIVERED, "차량 검증은 DELIVERED 상태에서만 가능합니다.");
        
        if (result == VerificationResult.PASSED) {
            this.status = EscrowStatus.VERIFIED;
        } else {
            this.status = EscrowStatus.VERIFICATION_FAILED;
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    public void confirmOwnershipTransfer() {
        validateState(EscrowStatus.VERIFIED, "명의 이전 확인은 VERIFIED 상태에서만 가능합니다.");
        
        this.status = EscrowStatus.OWNERSHIP_TRANSFERRED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void startSettlement() {
        validateState(EscrowStatus.OWNERSHIP_TRANSFERRED, "정산 시작은 OWNERSHIP_TRANSFERRED 상태에서만 가능합니다.");
        
        this.status = EscrowStatus.SETTLING;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void completeSettlement() {
        validateState(EscrowStatus.SETTLING, "정산 완료는 SETTLING 상태에서만 가능합니다.");
        
        this.status = EscrowStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markSettlementFailed() {
        validateState(EscrowStatus.SETTLING, "정산 실패 처리는 SETTLING 상태에서만 가능합니다.");
        
        this.status = EscrowStatus.SETTLEMENT_FAILED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void cancel(String reason) {
        if (this.status == EscrowStatus.SETTLING || this.status == EscrowStatus.COMPLETED) {
            throw new InvalidEscrowStateException("정산 중이거나 완료된 거래는 취소할 수 없습니다.");
        }
        
        this.status = EscrowStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void raiseDispute(String reason, String raisedBy) {
        if (this.status == EscrowStatus.COMPLETED || this.status == EscrowStatus.CANCELLED) {
            throw new InvalidEscrowStateException("완료되거나 취소된 거래는 분쟁을 제기할 수 없습니다.");
        }
        
        this.status = EscrowStatus.DISPUTED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void resolveDispute() {
        validateState(EscrowStatus.DISPUTED, "분쟁 해결은 DISPUTED 상태에서만 가능합니다.");
        // 분쟁 해결 후 상태는 DisputeService에서 결정
    }
    
    public BigDecimal calculateFee() {
        return this.amount.multiply(this.feeRate);
    }
    
    public BigDecimal calculateSellerAmount() {
        return this.amount.subtract(calculateFee());
    }
    
    private void validateState(EscrowStatus expectedStatus, String errorMessage) {
        if (this.status != expectedStatus) {
            throw new InvalidEscrowStateException(errorMessage + " 현재 상태: " + this.status);
        }
    }
    
    public boolean canCancel() {
        return this.status != EscrowStatus.SETTLING && 
               this.status != EscrowStatus.COMPLETED;
    }
    
    public boolean canRaiseDispute() {
        return this.status != EscrowStatus.COMPLETED && 
               this.status != EscrowStatus.CANCELLED;
    }
}
