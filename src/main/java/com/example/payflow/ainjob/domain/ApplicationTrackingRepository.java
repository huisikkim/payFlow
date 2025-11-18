package com.example.payflow.ainjob.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationTrackingRepository extends JpaRepository<ApplicationTracking, Long> {
    
    List<ApplicationTracking> findByApplicantId(Long applicantId);
    
    List<ApplicationTracking> findByJobPostingId(Long jobPostingId);
    
    Page<ApplicationTracking> findByJobPostingId(Long jobPostingId, Pageable pageable);
    
    Page<ApplicationTracking> findByJobPostingIdAndStatus(Long jobPostingId, ApplicationStatus status, Pageable pageable);
    
    Optional<ApplicationTracking> findByApplicantIdAndJobPostingId(Long applicantId, Long jobPostingId);
    
    @Query("SELECT at FROM ApplicationTracking at " +
           "LEFT JOIN FETCH at.statusHistories " +
           "WHERE at.id = :id")
    Optional<ApplicationTracking> findByIdWithHistories(@Param("id") Long id);
}
