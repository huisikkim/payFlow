package com.example.payflow.escrow.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VirtualAccountDepositRepository extends JpaRepository<VirtualAccountDeposit, Long> {
    
    List<VirtualAccountDeposit> findByTransactionId(String transactionId);
    
    Optional<VirtualAccountDeposit> findByOrderId(String orderId);
    
    Optional<VirtualAccountDeposit> findByPaymentKey(String paymentKey);
    
    List<VirtualAccountDeposit> findByStatus(VirtualAccountStatus status);
}
