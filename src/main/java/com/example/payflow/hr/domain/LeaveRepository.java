package com.example.payflow.hr.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRepository extends JpaRepository<Leave, Long> {
    
    List<Leave> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<Leave> findByStatusOrderByCreatedAtDesc(LeaveStatus status);
    
    @Query("SELECT l FROM Leave l WHERE l.userId = :userId " +
           "AND l.status = :status ORDER BY l.createdAt DESC")
    List<Leave> findByUserIdAndStatus(
        @Param("userId") String userId,
        @Param("status") LeaveStatus status
    );
    
    @Query("SELECT COALESCE(SUM(l.days), 0) FROM Leave l " +
           "WHERE l.userId = :userId AND l.status = 'APPROVED' " +
           "AND YEAR(l.startDate) = :year")
    Integer countUsedLeaveDays(
        @Param("userId") String userId,
        @Param("year") int year
    );
}
