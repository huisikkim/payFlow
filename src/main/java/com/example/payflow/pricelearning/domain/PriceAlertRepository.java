package com.example.payflow.pricelearning.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
    
    Optional<PriceAlert> findByAlertId(String alertId);
    
    List<PriceAlert> findByStatusOrderByCreatedAtDesc(PriceAlertStatus status);
    
    List<PriceAlert> findByItemNameOrderByCreatedAtDesc(String itemName);
    
    List<PriceAlert> findByOrderIdOrderByCreatedAtDesc(String orderId);
    
    List<PriceAlert> findTop10ByOrderByCreatedAtDesc();
}
