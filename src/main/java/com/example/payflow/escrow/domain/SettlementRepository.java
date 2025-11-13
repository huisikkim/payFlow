package com.example.payflow.escrow.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    
    Optional<Settlement> findByTransactionId(String transactionId);
    
    List<Settlement> findBySellerId(String sellerId);
    
    List<Settlement> findByCompletedAtIsNotNull();
    
    List<Settlement> findByCompletedAtIsNull();
    
    boolean existsByTransactionId(String transactionId);
}
