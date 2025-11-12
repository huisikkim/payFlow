package com.example.payflow.chatbot.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversationContextRepository extends JpaRepository<ConversationContext, Long> {
    Optional<ConversationContext> findByConversationId(Long conversationId);
}
