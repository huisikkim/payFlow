package com.example.payflow.sessionreplay.domain;

import java.util.List;

/**
 * 상호작용 이벤트 Repository 인터페이스
 */
public interface InteractionEventRepository {
    
    InteractionEvent save(InteractionEvent event);
    
    List<InteractionEvent> findBySessionIdOrderByTimestamp(String sessionId);
}
