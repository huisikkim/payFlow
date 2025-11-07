package com.example.payflow.stage.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StagePayoutRepository extends JpaRepository<StagePayout, Long> {
    
    List<StagePayout> findByStageId(Long stageId);
    
    List<StagePayout> findByUsername(String username);
    
    Optional<StagePayout> findByStageIdAndTurnNumber(Long stageId, Integer turnNumber);
    
    @Query("SELECT sp FROM StagePayout sp WHERE sp.isCompleted = false AND sp.scheduledAt <= :scheduledAt")
    List<StagePayout> findPendingPayoutsScheduledBefore(@Param("scheduledAt") LocalDateTime scheduledAt);
    
    @Query("SELECT COUNT(sp) FROM StagePayout sp WHERE sp.stage.id = :stageId AND sp.isCompleted = true")
    Long countCompletedPayoutsByStage(@Param("stageId") Long stageId);
}
