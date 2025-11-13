package com.example.payflow.escrow.application;

import com.example.payflow.escrow.domain.EscrowEventStore;
import com.example.payflow.escrow.domain.EscrowEventStoreRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EscrowEventSourcingService {
    
    private final EscrowEventStoreRepository eventStoreRepository;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public void storeEscrowEvent(String transactionId, String eventType, 
                                String previousStatus, String newStatus,
                                Map<String, Object> eventData, String triggeredBy) {
        
        // 다음 시퀀스 번호 계산
        Integer nextSequence = eventStoreRepository.findMaxSequenceByTransactionId(transactionId)
            .map(max -> max + 1)
            .orElse(1);
        
        // 이벤트 데이터를 JSON으로 변환
        String eventDataJson = null;
        if (eventData != null && !eventData.isEmpty()) {
            try {
                eventDataJson = objectMapper.writeValueAsString(eventData);
            } catch (JsonProcessingException e) {
                log.error("이벤트 데이터 직렬화 실패: transactionId={}", transactionId, e);
                eventDataJson = "{}";
            }
        }
        
        // 이벤트 저장
        EscrowEventStore event = new EscrowEventStore(
            transactionId,
            nextSequence,
            eventType,
            previousStatus,
            newStatus,
            eventDataJson,
            triggeredBy
        );
        
        eventStoreRepository.save(event);
        
        log.info("에스크로 이벤트 저장: transactionId={}, eventType={}, sequence={}", 
            transactionId, eventType, nextSequence);
    }
    
    @Transactional(readOnly = true)
    public List<EscrowEventStore> getEventHistory(String transactionId) {
        return eventStoreRepository.findByTransactionIdOrderBySequenceAsc(transactionId);
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> reconstructState(String transactionId, Integer upToSequence) {
        List<EscrowEventStore> events;
        
        if (upToSequence != null) {
            events = eventStoreRepository.findByTransactionIdAndSequenceLessThanEqualOrderBySequenceAsc(
                transactionId, upToSequence);
        } else {
            events = eventStoreRepository.findByTransactionIdOrderBySequenceAsc(transactionId);
        }
        
        Map<String, Object> state = new HashMap<>();
        state.put("transactionId", transactionId);
        state.put("eventCount", events.size());
        
        if (!events.isEmpty()) {
            EscrowEventStore lastEvent = events.get(events.size() - 1);
            state.put("currentStatus", lastEvent.getNewStatus());
            state.put("lastEventType", lastEvent.getEventType());
            state.put("lastEventSequence", lastEvent.getSequence());
            state.put("lastEventTime", lastEvent.getOccurredAt());
        }
        
        // 이벤트 히스토리 추가
        state.put("events", events);
        
        log.info("상태 재구성 완료: transactionId={}, upToSequence={}, eventCount={}", 
            transactionId, upToSequence, events.size());
        
        return state;
    }
    
    @Transactional(readOnly = true)
    public boolean hasEvents(String transactionId) {
        return eventStoreRepository.existsByTransactionId(transactionId);
    }
}
