package com.example.payflow.logging.application;

import com.example.payflow.logging.domain.PaymentEventStore;
import com.example.payflow.logging.domain.PaymentEventStoreRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 결제 이벤트 소싱 서비스
 * 결제의 모든 상태 변경을 이벤트로 저장하여 이력 추적 및 상태 재구성 가능
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventSourcingService {
    
    private final PaymentEventStoreRepository eventStoreRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 결제 상태 변경 이벤트 저장
     */
    @Transactional
    public PaymentEventStore storePaymentEvent(
        String paymentId,
        String eventType,
        String previousState,
        String newState,
        Object eventData,
        String actor
    ) {
        Integer nextSequence = eventStoreRepository.getNextSequence(paymentId);
        
        PaymentEventStore event = PaymentEventStore.builder()
            .paymentId(paymentId)
            .sequence(nextSequence)
            .eventType(eventType)
            .previousState(previousState)
            .newState(newState)
            .eventData(serializeData(eventData))
            .occurredAt(LocalDateTime.now())
            .actor(actor)
            .build();
        
        PaymentEventStore saved = eventStoreRepository.save(event);
        log.info("Payment event stored: {} - {} -> {} (seq: {})", 
            paymentId, previousState, newState, nextSequence);
        
        return saved;
    }
    
    /**
     * 결제 이벤트 히스토리 조회
     */
    @Transactional(readOnly = true)
    public List<PaymentEventStore> getPaymentHistory(String paymentId) {
        return eventStoreRepository.findByPaymentIdOrderBySequenceAsc(paymentId);
    }
    
    /**
     * 특정 시점의 결제 상태 재구성
     */
    @Transactional(readOnly = true)
    public String reconstructPaymentState(String paymentId, Integer upToSequence) {
        List<PaymentEventStore> events = eventStoreRepository
            .findByPaymentIdOrderBySequenceAsc(paymentId);
        
        String currentState = null;
        for (PaymentEventStore event : events) {
            if (event.getSequence() > upToSequence) {
                break;
            }
            currentState = event.getNewState();
        }
        
        return currentState;
    }
    
    /**
     * 결제의 최신 상태 조회
     */
    @Transactional(readOnly = true)
    public String getCurrentPaymentState(String paymentId) {
        PaymentEventStore latestEvent = eventStoreRepository
            .findFirstByPaymentIdOrderBySequenceDesc(paymentId);
        
        return latestEvent != null ? latestEvent.getNewState() : null;
    }
    
    private String serializeData(Object data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("Failed to serialize event data", e);
            return data.toString();
        }
    }
}
