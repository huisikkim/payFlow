package com.example.payflow.escrow.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    
    List<Dispute> findByTransactionId(String transactionId);
    
    List<Dispute> findByStatus(DisputeStatus status);
    
    List<Dispute> findByTransactionIdAndStatus(String transactionId, DisputeStatus status);
    
    List<Dispute> findByRaisedBy(String raisedBy);
    
    boolean existsByTransactionIdAndStatus(String transactionId, DisputeStatus status);
}
