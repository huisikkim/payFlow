package com.example.payflow.chatbot.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    Optional<Interview> findByConversationIdAndStatus(Long conversationId, InterviewStatus status);
    Optional<Interview> findByConversationId(Long conversationId);
}
