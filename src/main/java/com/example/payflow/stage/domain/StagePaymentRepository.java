package com.example.payflow.stage.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StagePaymentRepository extends JpaRepository<StagePayment, Long> {
    
    List<StagePayment> findByStageId(Long stageId);
    
    List<StagePayment> findByUsername(String username);
    
    List<StagePayment> findByStageIdAndUsername(Long stageId, String username);
    
    Optional<StagePayment> findByStageIdAndUsernameAndMonthNumber(Long stageId, String username, Integer monthNumber);
    
    @Query("SELECT sp FROM StagePayment sp WHERE sp.isPaid = false AND sp.dueDate <= :dueDate")
    List<StagePayment> findUnpaidPaymentsDueBefore(@Param("dueDate") LocalDateTime dueDate);
    
    @Query("SELECT COUNT(sp) FROM StagePayment sp WHERE sp.stage.id = :stageId AND sp.monthNumber = :monthNumber AND sp.isPaid = true")
    Long countPaidPaymentsByStageAndMonth(@Param("stageId") Long stageId, @Param("monthNumber") Integer monthNumber);
}
