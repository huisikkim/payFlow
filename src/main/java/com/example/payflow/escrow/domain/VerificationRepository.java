package com.example.payflow.escrow.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, Long> {
    
    List<Verification> findByTransactionId(String transactionId);
    
    List<Verification> findByTransactionIdAndType(String transactionId, VerificationType type);
    
    Optional<Verification> findFirstByTransactionIdAndTypeOrderByVerifiedAtDesc(
        String transactionId, VerificationType type);
    
    boolean existsByTransactionIdAndTypeAndResult(
        String transactionId, VerificationType type, VerificationResult result);
}
