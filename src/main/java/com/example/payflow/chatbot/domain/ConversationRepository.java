package com.example.payflow.chatbot.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<Conversation> findFirstByUserIdAndStatusOrderByCreatedAtDesc(String userId, ConversationStatus status);
}
