package com.example.payflow.parlevel.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PredictedOrderRepository extends JpaRepository<PredictedOrder, Long> {
    
    List<PredictedOrder> findByStoreIdAndStatus(String storeId, PredictionStatus status);
    
    List<PredictedOrder> findByStoreIdAndPredictedOrderDate(String storeId, LocalDate date);
    
    List<PredictedOrder> findByStoreIdOrderByCreatedAtDesc(String storeId);
}
