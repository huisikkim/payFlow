package com.example.payflow.pricelearning.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemPriceHistoryRepository extends JpaRepository<ItemPriceHistory, Long> {
    
    List<ItemPriceHistory> findByItemNameOrderByRecordedAtDesc(String itemName);
    
    List<ItemPriceHistory> findByItemNameAndRecordedAtAfterOrderByRecordedAtDesc(
        String itemName, LocalDateTime after);
    
    @Query("SELECT DISTINCT h.itemName FROM ItemPriceHistory h ORDER BY h.itemName")
    List<String> findAllDistinctItemNames();
    
    @Query("SELECT AVG(h.unitPrice) FROM ItemPriceHistory h " +
           "WHERE h.itemName = :itemName AND h.recordedAt >= :after")
    Double calculateAveragePrice(@Param("itemName") String itemName, 
                                 @Param("after") LocalDateTime after);
    
    @Query("SELECT AVG(h.unitPrice) FROM ItemPriceHistory h " +
           "WHERE h.itemName = :itemName AND h.distributorId = :distributorId " +
           "AND h.recordedAt >= :after")
    Double calculateDistributorAveragePrice(@Param("itemName") String itemName,
                                           @Param("distributorId") String distributorId,
                                           @Param("after") LocalDateTime after);
    
    @Query("SELECT MAX(h.unitPrice) FROM ItemPriceHistory h " +
           "WHERE h.itemName = :itemName AND h.recordedAt >= :after")
    Long findMaxPrice(@Param("itemName") String itemName, @Param("after") LocalDateTime after);
    
    @Query("SELECT MIN(h.unitPrice) FROM ItemPriceHistory h " +
           "WHERE h.itemName = :itemName AND h.recordedAt >= :after")
    Long findMinPrice(@Param("itemName") String itemName, @Param("after") LocalDateTime after);
}
