package com.example.payflow.settlement.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailySettlementRepository extends JpaRepository<DailySettlement, Long> {
    
    Optional<DailySettlement> findBySettlementDateAndStoreIdAndDistributorId(
        LocalDate settlementDate, String storeId, String distributorId);
    
    List<DailySettlement> findByStoreIdAndSettlementDateBetweenOrderBySettlementDateDesc(
        String storeId, LocalDate startDate, LocalDate endDate);
    
    List<DailySettlement> findByDistributorIdAndSettlementDateBetweenOrderBySettlementDateDesc(
        String distributorId, LocalDate startDate, LocalDate endDate);
    
    List<DailySettlement> findByStoreIdOrderBySettlementDateDesc(String storeId);
    
    List<DailySettlement> findByDistributorIdOrderBySettlementDateDesc(String distributorId);
    
    @Query("SELECT SUM(d.totalOutstandingAmount) FROM DailySettlement d " +
           "WHERE d.storeId = :storeId")
    Long calculateTotalOutstandingByStore(@Param("storeId") String storeId);
    
    @Query("SELECT SUM(d.totalOutstandingAmount) FROM DailySettlement d " +
           "WHERE d.distributorId = :distributorId")
    Long calculateTotalOutstandingByDistributor(@Param("distributorId") String distributorId);
    
    @Query("SELECT SUM(d.totalSalesAmount) FROM DailySettlement d " +
           "WHERE d.storeId = :storeId AND d.settlementDate BETWEEN :startDate AND :endDate")
    Long calculateTotalSalesByStoreAndPeriod(
        @Param("storeId") String storeId, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(d.totalSalesAmount) FROM DailySettlement d " +
           "WHERE d.distributorId = :distributorId AND d.settlementDate BETWEEN :startDate AND :endDate")
    Long calculateTotalSalesByDistributorAndPeriod(
        @Param("distributorId") String distributorId, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate);
}
