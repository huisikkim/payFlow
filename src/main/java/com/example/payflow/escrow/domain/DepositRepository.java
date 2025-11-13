package com.example.payflow.escrow.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepositRepository extends JpaRepository<Deposit, Long> {
    
    List<Deposit> findByTransactionId(String transactionId);
    
    List<Deposit> findByTransactionIdAndConfirmedAtIsNotNull(String transactionId);
    
    Optional<Deposit> findFirstByTransactionIdOrderByDepositedAtDesc(String transactionId);
}
