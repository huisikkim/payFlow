package com.example.payflow.stage.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StageParticipantRepository extends JpaRepository<StageParticipant, Long> {
    
    List<StageParticipant> findByStageId(Long stageId);
    
    List<StageParticipant> findByUsername(String username);
    
    Optional<StageParticipant> findByStageIdAndTurnNumber(Long stageId, Integer turnNumber);
    
    Optional<StageParticipant> findByStageIdAndUsername(Long stageId, String username);
    
    @Query("SELECT sp FROM StageParticipant sp WHERE sp.stage.id = :stageId AND sp.turnNumber = :turnNumber AND sp.hasReceivedPayout = false")
    Optional<StageParticipant> findPendingPayoutByStageAndTurn(@Param("stageId") Long stageId, @Param("turnNumber") Integer turnNumber);
    
    boolean existsByStageIdAndTurnNumber(Long stageId, Integer turnNumber);
    
    boolean existsByStageIdAndUsername(Long stageId, String username);
}
