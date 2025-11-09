package com.example.payflow.saga.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_sagas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderSaga {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String sagaId;
    
    @Column(nullable = false)
    private String orderId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaStep currentStep;
    
    private String paymentKey;
    private Long inventoryReservationId;
    
    @Column(length = 1000)
    private String errorMessage;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public OrderSaga(String sagaId, String orderId) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.status = SagaStatus.STARTED;
        this.currentStep = SagaStep.ORDER_CREATED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void moveToPaymentProcessed(String paymentKey) {
        this.paymentKey = paymentKey;
        this.currentStep = SagaStep.PAYMENT_PROCESSED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void moveToInventoryReserved(Long reservationId) {
        this.inventoryReservationId = reservationId;
        this.currentStep = SagaStep.INVENTORY_RESERVED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void complete() {
        this.status = SagaStatus.COMPLETED;
        this.currentStep = SagaStep.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void startCompensation(String errorMessage) {
        this.status = SagaStatus.COMPENSATING;
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void compensated() {
        this.status = SagaStatus.COMPENSATED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void fail(String errorMessage) {
        this.status = SagaStatus.FAILED;
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
    }
}
