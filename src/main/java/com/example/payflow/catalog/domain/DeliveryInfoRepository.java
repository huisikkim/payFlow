package com.example.payflow.catalog.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryInfoRepository extends JpaRepository<DeliveryInfo, Long> {
    
    Optional<DeliveryInfo> findByOrderId(Long orderId);
    
    List<DeliveryInfo> findByStatus(DeliveryStatus status);
    
    List<DeliveryInfo> findByOrder_DistributorIdOrderByCreatedAtDesc(String distributorId);
    
    List<DeliveryInfo> findByOrder_StoreIdOrderByCreatedAtDesc(String storeId);
}
