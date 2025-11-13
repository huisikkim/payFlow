package com.example.payflow.escrow.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EscrowEventStoreRepository extends JpaRepository<EscrowEventStore, Long> {
    
    List<EscrowEventStore> findByTransactionIdOrderBySequenceAsc(String transactionId);
    
    List<EscrowEventStore> findByTransactionIdAndSequenceLessThanEqualOrderBySequenceAsc(
        String transactionId, Integer sequence);
    
    @Query("SELECT MAX(e.sequence) FROM EscrowEventStore e WHERE e.transactionId = :transactionId")
    Optional<Integer> findMaxSequenceByTransactionId(String transactionId);
    
    boolean existsByTransactionId(String transactionId);
}
