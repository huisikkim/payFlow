package com.example.payflow.settlement.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientSettlementRepository extends JpaRepository<IngredientSettlement, Long> {
    Optional<IngredientSettlement> findBySettlementId(String settlementId);
    Optional<IngredientSettlement> findByOrderId(String orderId);
    List<IngredientSettlement> findByStoreId(String storeId);
    List<IngredientSettlement> findByDistributorId(String distributorId);
    
    @Query("SELECT SUM(s.outstandingAmount) FROM IngredientSettlement s WHERE s.storeId = :storeId AND s.status = 'COMPLETED'")
    Long calculateTotalOutstandingByStore(String storeId);
    
    @Query("SELECT s FROM IngredientSettlement s WHERE s.settlementDate >= :startDateTime AND s.settlementDate < :endDateTime")
    List<IngredientSettlement> findBySettlementDateBetween(
        java.time.LocalDateTime startDateTime, 
        java.time.LocalDateTime endDateTime);
}
