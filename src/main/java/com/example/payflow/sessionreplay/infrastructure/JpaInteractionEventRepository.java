package com.example.payflow.sessionreplay.infrastructure;

import com.example.payflow.sessionreplay.domain.InteractionEvent;
import com.example.payflow.sessionreplay.domain.InteractionEventRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 상호작용 이벤트 JPA Repository 구현
 */
@Repository
public interface JpaInteractionEventRepository extends JpaRepository<InteractionEvent, Long>, InteractionEventRepository {
    
    @Override
    List<InteractionEvent> findBySessionIdOrderByTimestamp(String sessionId);
}
