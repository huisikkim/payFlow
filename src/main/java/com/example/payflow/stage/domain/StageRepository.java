package com.example.payflow.stage.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StageRepository extends JpaRepository<Stage, Long> {
    
    List<Stage> findByStatus(StageStatus status);
    
    @Query("SELECT s FROM Stage s LEFT JOIN FETCH s.participants WHERE s.id = :id")
    Optional<Stage> findByIdWithParticipants(@Param("id") Long id);
    
    @Query("SELECT s FROM Stage s WHERE s.status = :status AND s.paymentDay = :day")
    List<Stage> findByStatusAndPaymentDay(@Param("status") StageStatus status, @Param("day") Integer day);
}
