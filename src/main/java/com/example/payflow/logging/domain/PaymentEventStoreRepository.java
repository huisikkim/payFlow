package com.example.payflow.logging.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PaymentEventStoreRepository extends JpaRepository<PaymentEventStore, Long> {
    
    /**
     * 결제 ID로 모든 이벤트 조회 (시간순)
     */
    List<PaymentEventStore> findByPaymentIdOrderBySequenceAsc(String paymentId);
    
    /**
     * 결제 ID의 최신 이벤트 조회
     */
    PaymentEventStore findFirstByPaymentIdOrderBySequenceDesc(String paymentId);
    
    /**
     * 결제 ID의 다음 시퀀스 번호 조회
     */
    @Query("SELECT COALESCE(MAX(p.sequence), 0) + 1 FROM PaymentEventStore p WHERE p.paymentId = :paymentId")
    Integer getNextSequence(String paymentId);
    
    /**
     * 특정 상태로 전이된 결제 목록
     */
    List<PaymentEventStore> findByNewStateOrderByOccurredAtDesc(String newState);
}
