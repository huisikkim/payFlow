package com.example.payflow.matching.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuoteRequestRepository extends JpaRepository<QuoteRequest, Long> {
    
    /**
     * 매장별 견적 요청 목록 조회
     */
    List<QuoteRequest> findByStoreIdOrderByRequestedAtDesc(String storeId);
    
    /**
     * 유통업체별 견적 요청 목록 조회
     */
    List<QuoteRequest> findByDistributorIdOrderByRequestedAtDesc(String distributorId);
    
    /**
     * 매장의 특정 상태 견적 요청 조회
     */
    List<QuoteRequest> findByStoreIdAndStatus(String storeId, QuoteRequest.QuoteStatus status);
    
    /**
     * 유통업체의 특정 상태 견적 요청 조회
     */
    List<QuoteRequest> findByDistributorIdAndStatus(String distributorId, QuoteRequest.QuoteStatus status);
}
