package com.example.payflow.ainjob.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AinjobJobPostingRepository extends JpaRepository<AinjobJobPosting, Long> {
    
    List<AinjobJobPosting> findByCompanyId(Long companyId);
    
    List<AinjobJobPosting> findByStatus(JobPostingStatus status);
    
    @Query("SELECT jp FROM AinjobJobPosting jp " +
           "LEFT JOIN FETCH jp.requiredSkills " +
           "WHERE jp.id = :id")
    Optional<AinjobJobPosting> findByIdWithSkills(@Param("id") Long id);
}
