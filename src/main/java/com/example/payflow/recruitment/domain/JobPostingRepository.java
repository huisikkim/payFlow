package com.example.payflow.recruitment.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    
    List<JobPosting> findByStatus(JobPostingStatus status);
    
    @Query("SELECT jp FROM JobPosting jp WHERE jp.status = :status AND jp.endDate >= :today")
    List<JobPosting> findActivePostings(@Param("status") JobPostingStatus status, 
                                       @Param("today") LocalDate today);
    
    @Query("SELECT jp FROM JobPosting jp WHERE jp.department.id = :departmentId")
    List<JobPosting> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT DISTINCT jp FROM JobPosting jp " +
           "JOIN jp.requirements r " +
           "WHERE r.skill.id = :skillId")
    List<JobPosting> findByRequiredSkill(@Param("skillId") Long skillId);
}
