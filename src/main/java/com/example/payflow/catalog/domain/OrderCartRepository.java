package com.example.payflow.catalog.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderCartRepository extends JpaRepository<OrderCart, Long> {
    
    /**
     * 매장의 특정 유통업체 장바구니 조회
     */
    Optional<OrderCart> findByStoreIdAndDistributorId(String storeId, String distributorId);
    
    /**
     * 매장의 장바구니 삭제
     */
    void deleteByStoreIdAndDistributorId(String storeId, String distributorId);
}
