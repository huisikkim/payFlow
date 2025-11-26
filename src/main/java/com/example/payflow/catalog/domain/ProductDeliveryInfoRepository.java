package com.example.payflow.catalog.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductDeliveryInfoRepository extends JpaRepository<ProductDeliveryInfo, Long> {
    
    Optional<ProductDeliveryInfo> findByProductId(Long productId);
    
    void deleteByProductId(Long productId);
}
