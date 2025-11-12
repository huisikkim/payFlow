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
    private final JobSearchService jobSearchService;

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

        // 2. 대화 컨텍스트 가져오기
        ConversationContext context = jobSearchService.getOrCreateContext(conversation.getId());

        // 3. 사용자 메시지 저장
        Intent detectedIntent = intentMatcher.detectIntent(request.getMessage());
        Message userMessage = new Message(MessageRole.USER, request.getMessage(), detectedIntent);
        conversation.addMessage(userMessage);

        // 4. 대화 흐름에 따른 응답 생성
        String responseText = processConversationFlow(conversation.getId(), context, request.getMessage(), detectedIntent);
        
        Message botMessage = new Message(MessageRole.BOT, responseText, detectedIntent);
        conversation.addMessage(botMessage);

        conversationRepository.save(conversation);

        // 5. 이벤트 발행 (EDA)
        publishMessageEvent(conversation.getId(), request.getMessage(), detectedIntent);

        log.info("Chat response generated with intent: {} at step: {}", detectedIntent, context.getCurrentStep());

        return ChatResponse.builder()
            .conversationId(conversation.getId())
            .message(responseText)
            .intent(detectedIntent.name())
            .build();
    }

    private String processConversationFlow(Long conversationId, ConversationContext context, 
                                          String message, Intent intent) {
        // 재시작 요청 처리
        if (intent == Intent.RESTART_SEARCH) {
            jobSearchService.resetContext(conversationId);
            return responseGenerator.generate(Intent.RESTART_SEARCH);
        }

        // 도움말 요청
        if (intent == Intent.HELP) {
            return responseGenerator.generate(Intent.HELP);
        }

        // 대화 단계별 처리
        switch (context.getCurrentStep()) {
            case INITIAL:
                return handleInitialStep(conversationId, context, message, intent);
            
            case ASKING_REGION:
                return handleRegionStep(conversationId, context, message);
            
            case ASKING_INDUSTRY:
                return handleIndustryStep(conversationId, context, message);
            
            case ASKING_SALARY:
                return handleSalaryStep(conversationId, context, message);
            
            case SHOWING_RESULTS:
                return handleResultsStep(context);
            
            default:
                return responseGenerator.generate(Intent.UNKNOWN);
        }
    }

    private String handleInitialStep(Long conversationId, ConversationContext context, 
                                     String message, Intent intent) {
        if (intent == Intent.GREETING) {
            return responseGenerator.generate(Intent.GREETING);
        }
        
        if (intent == Intent.JOB_SEARCH_START) {
            context.moveToStep(ConversationStep.ASKING_REGION);
            jobSearchService.getOrCreateContext(conversationId); // 컨텍스트 저장
            return responseGenerator.generate(Intent.JOB_SEARCH_START);
        }
        
        return responseGenerator.generate(Intent.UNKNOWN);
    }

    private String handleRegionStep(Long conversationId, ConversationContext context, String message) {
        String region = jobSearchService.extractRegion(message);
        
        if (region != null) {
            jobSearchService.updateRegion(conversationId, region);
            List<String> industries = jobSearchService.getAvailableIndustries();
            
            return String.format("'%s' 지역을 선택하셨네요! 👍\n\n" +
                "다음으로, 어떤 업종에 관심이 있으신가요?\n" +
                "선택 가능한 업종: %s", 
                region, String.join(", ", industries));
        }
        
        List<String> regions = jobSearchService.getAvailableRegions();
        return String.format("죄송해요, 해당 지역을 찾을 수 없어요. 😅\n\n" +
            "다음 지역 중에서 선택해주세요:\n%s", 
            String.join(", ", regions));
    }

    private String handleIndustryStep(Long conversationId, ConversationContext context, String message) {
        String industry = jobSearchService.extractIndustry(message);
        
        if (industry != null) {
            jobSearchService.updateIndustry(conversationId, industry);
            
            return String.format("'%s' 업종을 선택하셨네요! 💼\n\n" +
                "마지막으로, 희망 연봉 범위를 알려주세요.\n" +
                "(예: 3000만원~5000만원, 4000만원 이상 등)", 
                industry);
        }
        
        List<String> industries = jobSearchService.getAvailableIndustries();
        return String.format("죄송해요, 해당 업종을 찾을 수 없어요. 😅\n\n" +
            "다음 업종 중에서 선택해주세요:\n%s", 
            String.join(", ", industries));
    }

    private String handleSalaryStep(Long conversationId, ConversationContext context, String message) {
        Long[] salaryRange = jobSearchService.extractSalaryRange(message);
        
        if (salaryRange != null) {
            jobSearchService.updateSalary(conversationId, salaryRange[0], salaryRange[1]);
            return handleResultsStep(context);
        }
        
        return "연봉 정보를 정확히 이해하지 못했어요. 😅\n\n" +
               "다시 입력해주세요.\n" +
               "(예: 3000만원~5000만원, 4000만원 이상)";
    }

    private String handleResultsStep(ConversationContext context) {
        List<Job> jobs = jobSearchService.searchJobs(context);
        
        if (jobs.isEmpty()) {
            return String.format("😢 죄송합니다. 조건에 맞는 채용 공고를 찾지 못했어요.\n\n" +
                "검색 조건:\n" +
                "• 지역: %s\n" +
                "• 업종: %s\n" +
                "• 연봉: %,d만원 ~ %,d만원\n\n" +
                "'다시'라고 입력하시면 새로운 검색을 시작할 수 있어요!",
                context.getSelectedRegion(),
                context.getSelectedIndustry(),
                context.getMinSalary() / 10000,
                context.getMaxSalary() / 10000);
        }
        
        StringBuilder result = new StringBuilder();
        result.append(String.format("🎉 총 %d개의 채용 공고를 찾았습니다!\n\n", jobs.size()));
        
        int count = 0;
        for (Job job : jobs) {
            if (count >= 5) break; // 최대 5개만 표시
            
            result.append(String.format("━━━━━━━━━━━━━━━━━━━━\n"));
            result.append(String.format("📌 %s\n", job.getCompanyName()));
            result.append(String.format("💼 %s\n", job.getPosition()));
            result.append(String.format("📍 %s | %s\n", job.getRegion(), job.getIndustry()));
            result.append(String.format("💰 %s\n", job.getSalaryRange()));
            result.append(String.format("📝 %s\n\n", job.getDescription()));
            count++;
        }
        
        if (jobs.size() > 5) {
            result.append(String.format("... 외 %d개 공고가 더 있습니다.\n\n", jobs.size() - 5));
        }
        
        result.append("'다시'라고 입력하시면 새로운 검색을 시작할 수 있어요!");
        
        return result.toString();
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
