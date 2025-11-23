package com.example.payflow.ingredientorder.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientOrderRepository extends JpaRepository<IngredientOrder, Long> {
    Optional<IngredientOrder> findByOrderId(String orderId);
    List<IngredientOrder> findByStoreId(String storeId);
    List<IngredientOrder> findByDistributorId(String distributorId);
    
    @Query("SELECT io FROM IngredientOrder io WHERE io.distributorId = :distributorId AND io.status = :status")
    List<IngredientOrder> findByDistributorIdAndStatus(String distributorId, IngredientOrderStatus status);
}
