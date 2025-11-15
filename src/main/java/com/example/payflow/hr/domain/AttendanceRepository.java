package com.example.payflow.hr.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    List<Attendance> findByUserIdOrderByCheckInTimeDesc(String userId);
    
    @Query("SELECT a FROM Attendance a WHERE a.userId = :userId " +
           "AND a.checkInTime >= :startDate AND a.checkInTime < :endDate")
    List<Attendance> findByUserIdAndDateRange(
        @Param("userId") String userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT a FROM Attendance a WHERE a.userId = :userId " +
           "AND a.status = 'CHECKED_IN' ORDER BY a.checkInTime DESC")
    Optional<Attendance> findActiveCheckIn(@Param("userId") String userId);
}
