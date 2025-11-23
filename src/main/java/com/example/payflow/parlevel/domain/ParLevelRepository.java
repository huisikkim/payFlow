package com.example.payflow.parlevel.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParLevelRepository extends JpaRepository<ParLevel, Long> {
    
    Optional<ParLevel> findByStoreIdAndItemName(String storeId, String itemName);
    
    List<ParLevel> findByStoreId(String storeId);
    
    List<ParLevel> findByStoreIdAndAutoOrderEnabled(String storeId, Boolean autoOrderEnabled);
}
