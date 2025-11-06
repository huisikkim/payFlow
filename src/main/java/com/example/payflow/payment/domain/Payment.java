package com.example.payflow.payment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String paymentKey;
    
    @Column(nullable = false)
    private String orderId;
    
    @Column(nullable = false)
    private String orderName;
    
    @Column(nullable = false)
    private Long amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
    
    private String method;
    private String customerEmail;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    
    public Payment(String orderId, String orderName, Long amount, String customerEmail) {
        this.orderId = orderId;
        this.orderName = orderName;
        this.amount = amount;
        this.customerEmail = customerEmail;
        this.status = PaymentStatus.READY;
        this.createdAt = LocalDateTime.now();
    }
    
    public void approve(String paymentKey, String method) {
        this.paymentKey = paymentKey;
        this.method = method;
        this.status = PaymentStatus.DONE;
        this.approvedAt = LocalDateTime.now();
    }
    
    public void fail() {
        this.status = PaymentStatus.FAILED;
    }
    
    public void cancel() {
        this.status = PaymentStatus.CANCELED;
    }
}
