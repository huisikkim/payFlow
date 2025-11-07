package com.example.payflow.stage.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipantSettlementRepository extends JpaRepository<ParticipantSettlement, Long> {
    
    List<ParticipantSettlement> findBySettlementId(Long settlementId);
    
    List<ParticipantSettlement> findByUsername(String username);
    
    Optional<ParticipantSettlement> findBySettlementIdAndUsername(Long settlementId, String username);
    
    @Query("SELECT ps FROM ParticipantSettlement ps WHERE ps.settlement.stage.id = :stageId AND ps.username = :username")
    Optional<ParticipantSettlement> findByStageIdAndUsername(@Param("stageId") Long stageId, @Param("username") String username);
}
