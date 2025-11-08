package com.example.payflow.chatbot.application;

import com.example.payflow.chatbot.domain.*;
import com.example.payflow.chatbot.application.dto.ChatRequest;
import com.example.payflow.chatbot.application.dto.ChatResponse;
import com.example.payflow.common.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ConversationRepository conversationRepository;
    private final IntentMatcher intentMatcher;
    private final ResponseGenerator responseGenerator;
    private final EventPublisher eventPublisher;

    @Transactional
    public ChatResponse chat(ChatRequest request) {
        log.info("Processing chat message from user: {}", request.getUserId());

        // 1. 활성 대화 찾기 또는 생성
        Conversation conversation = conversationRepository
            .findFirstByUserIdAndStatusOrderByCreatedAtDesc(request.getUserId(), ConversationStatus.ACTIVE)
            .orElseGet(() -> {
                Conversation newConversation = new Conversation(request.getUserId());
                return conversationRepository.save(newConversation);
            });

        // 2. 사용자 메시지 저장
        Intent detectedIntent = intentMatcher.detectIntent(request.getMessage());
        Message userMessage = new Message(MessageRole.USER, request.getMessage(), detectedIntent);
        conversation.addMessage(userMessage);

        // 3. 응답 생성
        String responseText = responseGenerator.generate(detectedIntent);
        Message botMessage = new Message(MessageRole.BOT, responseText, detectedIntent);
        conversation.addMessage(botMessage);

        conversationRepository.save(conversation);

        // 4. 이벤트 발행 (EDA)
        publishMessageEvent(conversation.getId(), request.getMessage(), detectedIntent);

        // 5. 에스컬레이션 체크
        if (shouldEscalate(conversation)) {
            conversation.escalate();
            conversationRepository.save(conversation);
            publishEscalationEvent(conversation.getId(), "Multiple unknown intents detected");
        }

        log.info("Chat response generated with intent: {}", detectedIntent);

        return ChatResponse.builder()
            .conversationId(conversation.getId())
            .message(responseText)
            .intent(detectedIntent.name())
            .build();
    }

    @Transactional(readOnly = true)
    public List<ChatResponse> getConversationHistory(String userId, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if (!conversation.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to conversation");
        }

        return conversation.getMessages().stream()
            .map(msg -> ChatResponse.builder()
                .conversationId(conversationId)
                .message(msg.getContent())
                .intent(msg.getIntent() != null ? msg.getIntent().name() : null)
                .role(msg.getRole().name())
                .timestamp(msg.getCreatedAt())
                .build())
            .collect(Collectors.toList());
    }

    @Transactional
    public void closeConversation(String userId, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if (!conversation.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to conversation");
        }

        conversation.close();
        conversationRepository.save(conversation);
    }

    private boolean shouldEscalate(Conversation conversation) {
        long unknownCount = conversation.getMessages().stream()
            .filter(msg -> msg.getRole() == MessageRole.USER)
            .filter(msg -> msg.getIntent() == Intent.UNKNOWN)
            .count();
        return unknownCount >= 3;
    }

    private void publishMessageEvent(Long conversationId, String message, Intent intent) {
        try {
            eventPublisher.publish(new com.example.payflow.chatbot.domain.event.MessageReceivedEvent(
                conversationId, message, intent
            ));
        } catch (Exception e) {
            log.error("Failed to publish message event", e);
        }
    }

    private void publishEscalationEvent(Long conversationId, String reason) {
        try {
            eventPublisher.publish(new com.example.payflow.chatbot.domain.event.EscalationRequiredEvent(
                conversationId, reason
            ));
        } catch (Exception e) {
            log.error("Failed to publish escalation event", e);
        }
    }
}
