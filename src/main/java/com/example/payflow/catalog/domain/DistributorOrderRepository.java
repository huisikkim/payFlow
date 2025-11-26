package com.example.payflow.catalog.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DistributorOrderRepository extends JpaRepository<DistributorOrder, Long> {
    
    List<DistributorOrder> findByStoreIdOrderByOrderedAtDesc(String storeId);
    
    List<DistributorOrder> findByStoreIdAndDistributorIdOrderByOrderedAtDesc(String storeId, String distributorId);
    
    Optional<DistributorOrder> findByOrderNumber(String orderNumber);
    
    List<DistributorOrder> findByStoreIdAndStatusOrderByOrderedAtDesc(String storeId, OrderStatus status);
}
