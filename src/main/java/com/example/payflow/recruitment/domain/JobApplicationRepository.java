package com.example.payflow.recruitment.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    
    List<JobApplication> findByJobPostingId(Long jobPostingId);
    
    List<JobApplication> findByCandidateId(Long candidateId);
    
    @Query("SELECT ja FROM JobApplication ja " +
           "WHERE ja.jobPosting.id = :jobPostingId " +
           "ORDER BY ja.matchingScore DESC")
    List<JobApplication> findByJobPostingIdOrderByMatchingScoreDesc(@Param("jobPostingId") Long jobPostingId);
    
    @Query("SELECT ja FROM JobApplication ja " +
           "WHERE ja.jobPosting.id = :jobPostingId AND ja.candidate.id = :candidateId")
    Optional<JobApplication> findByJobPostingAndCandidate(@Param("jobPostingId") Long jobPostingId,
                                                          @Param("candidateId") Long candidateId);
    
    List<JobApplication> findByStatus(ApplicationStatus status);
}
