package com.example.payflow.stage.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StageSettlementRepository extends JpaRepository<StageSettlement, Long> {
    
    Optional<StageSettlement> findByStageId(Long stageId);
    
    @Query("SELECT s FROM StageSettlement s LEFT JOIN FETCH s.participantSettlements WHERE s.stage.id = :stageId")
    Optional<StageSettlement> findByStageIdWithParticipants(@Param("stageId") Long stageId);
    
    boolean existsByStageId(Long stageId);
}
