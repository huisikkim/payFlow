package com.example.payflow.escrow.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EscrowTransactionRepository extends JpaRepository<EscrowTransaction, Long> {
    
    Optional<EscrowTransaction> findByTransactionId(String transactionId);
    
    List<EscrowTransaction> findByBuyerUserId(String buyerUserId);
    
    List<EscrowTransaction> findBySellerUserId(String sellerUserId);
    
    List<EscrowTransaction> findByStatus(EscrowStatus status);
    
    boolean existsByTransactionId(String transactionId);
}
