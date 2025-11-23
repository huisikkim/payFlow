package com.example.payflow.parlevel.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ConsumptionPatternRepository extends JpaRepository<ConsumptionPattern, Long> {
    
    List<ConsumptionPattern> findByStoreIdAndItemNameAndConsumptionDateBetween(
        String storeId, String itemName, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT AVG(c.quantity) FROM ConsumptionPattern c " +
           "WHERE c.storeId = :storeId AND c.itemName = :itemName " +
           "AND c.consumptionDate >= :startDate")
    Double calculateAverageConsumption(@Param("storeId") String storeId,
                                      @Param("itemName") String itemName,
                                      @Param("startDate") LocalDate startDate);
    
    @Query("SELECT AVG(c.quantity) FROM ConsumptionPattern c " +
           "WHERE c.storeId = :storeId AND c.itemName = :itemName " +
           "AND c.dayOfWeek = :dayOfWeek " +
           "AND c.consumptionDate >= :startDate")
    Double calculateAverageByDayOfWeek(@Param("storeId") String storeId,
                                       @Param("itemName") String itemName,
                                       @Param("dayOfWeek") DayOfWeek dayOfWeek,
                                       @Param("startDate") LocalDate startDate);
    
    @Query("SELECT STDDEV(c.quantity) FROM ConsumptionPattern c " +
           "WHERE c.storeId = :storeId AND c.itemName = :itemName " +
           "AND c.consumptionDate >= :startDate")
    Double calculateStandardDeviation(@Param("storeId") String storeId,
                                      @Param("itemName") String itemName,
                                      @Param("startDate") LocalDate startDate);
}
