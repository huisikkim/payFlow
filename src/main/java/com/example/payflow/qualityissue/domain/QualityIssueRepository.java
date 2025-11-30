package com.example.payflow.qualityissue.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QualityIssueRepository extends JpaRepository<QualityIssue, Long> {
    
    List<QualityIssue> findByStoreIdOrderBySubmittedAtDesc(String storeId);
    
    List<QualityIssue> findByDistributorIdOrderBySubmittedAtDesc(String distributorId);
    
    List<QualityIssue> findByOrderId(Long orderId);
    
    List<QualityIssue> findByStatus(IssueStatus status);
    
    List<QualityIssue> findByDistributorIdAndStatus(String distributorId, IssueStatus status);
}
