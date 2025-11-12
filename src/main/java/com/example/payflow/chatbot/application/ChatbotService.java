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
    private final InterviewService interviewService;

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
                return handleResultsStep(conversationId, context, message);
            
            case ASKING_JOB_SELECTION:
                return handleJobSelectionStep(conversationId, context, message);
            
            case ASKING_TECH_STACK:
                return handleTechStackStep(conversationId, context, message);
            
            case CONDUCTING_INTERVIEW:
                return handleInterviewStep(conversationId, context, message);
            
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
            return handleResultsStep(conversationId, context, message);
        }
        
        return "연봉 정보를 정확히 이해하지 못했어요. 😅\n\n" +
               "다시 입력해주세요.\n" +
               "(예: 3000만원~5000만원, 4000만원 이상)";
    }

    private String handleResultsStep(Long conversationId, ConversationContext context, String message) {
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
        
        // 결과를 컨텍스트에 저장 (나중에 선택할 수 있도록)
        jobSearchService.saveSearchResults(conversationId, jobs);
        
        StringBuilder result = new StringBuilder();
        result.append(String.format("🎉 총 %d개의 채용 공고를 찾았습니다!\n\n", jobs.size()));
        
        int count = 0;
        for (Job job : jobs) {
            if (count >= 5) break; // 최대 5개만 표시
            
            result.append(String.format("━━━━━━━━━━━━━━━━━━━━\n"));
            result.append(String.format("[%d] %s\n", count + 1, job.getCompanyName()));
            result.append(String.format("%s\n", job.getPosition()));
            result.append(String.format("%s | %s\n", job.getRegion(), job.getIndustry()));
            result.append(String.format("%s\n", job.getSalaryRange()));
            result.append(String.format("%s\n\n", job.getDescription()));
            count++;
        }
        
        if (jobs.size() > 5) {
            result.append(String.format("... 외 %d개 공고가 더 있습니다.\n\n", jobs.size() - 5));
        }
        
        result.append("💡 관심있는 공고가 있으신가요?\n");
        result.append("번호를 입력하시면 해당 포지션에 대한 모의 면접을 진행할 수 있습니다!\n");
        result.append("(예: 1번, 2번 면접)\n\n");
        result.append("'다시'라고 입력하시면 새로운 검색을 시작할 수 있어요!");
        
        context.moveToStep(ConversationStep.ASKING_JOB_SELECTION);
        jobSearchService.getOrCreateContext(conversationId);
        
        return result.toString();
    }

    private String handleJobSelectionStep(Long conversationId, ConversationContext context, String message) {
        // 번호 추출
        Integer jobNumber = extractJobNumber(message);
        
        if (jobNumber == null) {
            return "공고 번호를 정확히 입력해주세요.\n(예: 1번, 2번 면접)";
        }
        
        List<Job> searchResults = jobSearchService.getSearchResults(conversationId);
        if (searchResults == null || jobNumber < 1 || jobNumber > searchResults.size()) {
            return String.format("1번부터 %d번 사이의 번호를 입력해주세요.", 
                searchResults != null ? searchResults.size() : 0);
        }
        
        Job selectedJob = searchResults.get(jobNumber - 1);
        context.setSelectedJobId(selectedJob.getId());
        context.moveToStep(ConversationStep.ASKING_TECH_STACK);
        jobSearchService.getOrCreateContext(conversationId);
        
        return String.format("'%s - %s' 포지션을 선택하셨네요! 👍\n\n" +
            "모의 면접을 시작하기 전에, 보유하신 기술 스택을 알려주세요.\n" +
            "여러 개를 쉼표(,)로 구분하여 입력해주세요.\n\n" +
            "예: Java, Spring Boot, MySQL, AWS",
            selectedJob.getCompanyName(), selectedJob.getPosition());
    }

    private String handleTechStackStep(Long conversationId, ConversationContext context, String message) {
        // 기술 스택 파싱
        List<String> techStacks = parseTechStacks(message);
        
        if (techStacks.isEmpty()) {
            return "기술 스택을 입력해주세요.\n예: Java, Spring Boot, MySQL";
        }
        
        // 기술 스택 저장
        techStacks.forEach(context::addTechStack);
        jobSearchService.getOrCreateContext(conversationId);
        
        // 면접 시작
        Interview interview = interviewService.startInterview(
            conversationId, 
            context.getSelectedJobId(), 
            conversationRepository.findById(conversationId)
                .map(Conversation::getUserId)
                .orElse("unknown"),
            techStacks
        );
        
        context.setCurrentInterviewId(interview.getId());
        context.moveToStep(ConversationStep.CONDUCTING_INTERVIEW);
        jobSearchService.getOrCreateContext(conversationId);
        
        // 첫 번째 질문 가져오기
        InterviewQuestion firstQuestion = interviewService.getNextQuestion(interview.getId());
        
        return String.format("좋습니다! 기술 스택: %s\n\n" +
            "🎤 모의 면접을 시작하겠습니다!\n" +
            "총 %d개의 질문이 준비되어 있습니다.\n\n" +
            "━━━━━━━━━━━━━━━━━━━━\n" +
            "질문 %d/%d [%s]\n\n" +
            "%s\n\n" +
            "답변을 입력해주세요:",
            String.join(", ", techStacks),
            interview.getQuestions().size(),
            firstQuestion.getQuestionNumber(),
            interview.getQuestions().size(),
            getCategoryName(firstQuestion.getCategory()),
            firstQuestion.getQuestion());
    }

    private String handleInterviewStep(Long conversationId, ConversationContext context, String message) {
        Long interviewId = context.getCurrentInterviewId();
        if (interviewId == null) {
            return "면접 정보를 찾을 수 없습니다. 처음부터 다시 시작해주세요.";
        }
        
        // 현재 질문 가져오기
        InterviewQuestion currentQuestion = interviewService.getNextQuestion(interviewId);
        
        if (currentQuestion == null) {
            // 모든 질문에 답변 완료 - 결과 표시
            InterviewService.InterviewResult result = interviewService.completeInterview(interviewId);
            context.moveToStep(ConversationStep.SHOWING_INTERVIEW_RESULT);
            jobSearchService.getOrCreateContext(conversationId);
            
            return formatInterviewResult(result);
        }
        
        // 이전 질문에 대한 답변 저장 (첫 질문이 아닌 경우)
        if (currentQuestion.getQuestionNumber() > 1) {
            InterviewQuestion prevQuestion = interviewService.getNextQuestion(interviewId);
            if (prevQuestion != null && prevQuestion.getQuestionNumber() == currentQuestion.getQuestionNumber() - 1) {
                interviewService.answerQuestion(interviewId, prevQuestion.getQuestionNumber(), message);
            }
        }
        
        // 답변 저장
        interviewService.answerQuestion(interviewId, currentQuestion.getQuestionNumber(), message);
        
        // 다음 질문 가져오기
        InterviewQuestion nextQuestion = interviewService.getNextQuestion(interviewId);
        
        if (nextQuestion == null) {
            // 마지막 질문 완료 - 결과 표시
            InterviewService.InterviewResult result = interviewService.completeInterview(interviewId);
            context.moveToStep(ConversationStep.SHOWING_INTERVIEW_RESULT);
            jobSearchService.getOrCreateContext(conversationId);
            
            return "답변 감사합니다! 😊\n\n" + formatInterviewResult(result);
        }
        
        // 다음 질문 표시
        Interview interview = interviewService.getInterviewByConversation(conversationId)
            .orElseThrow(() -> new IllegalArgumentException("Interview not found"));
        
        return String.format("답변 감사합니다! 😊\n\n" +
            "━━━━━━━━━━━━━━━━━━━━\n" +
            "질문 %d/%d [%s]\n\n" +
            "%s\n\n" +
            "답변을 입력해주세요:",
            nextQuestion.getQuestionNumber(),
            interview.getQuestions().size(),
            getCategoryName(nextQuestion.getCategory()),
            nextQuestion.getQuestion());
    }

    private String formatInterviewResult(InterviewService.InterviewResult result) {
        return String.format(
            "🎊 면접이 완료되었습니다!\n\n" +
            "━━━━━━━━━━━━━━━━━━━━\n" +
            "면접 결과\n" +
            "━━━━━━━━━━━━━━━━━━━━\n\n" +
            "답변한 질문: %d/%d\n" +
            "총점: %d점\n" +
            "평균 점수: %d점\n" +
            "합격 예상률: %.1f%%\n\n" +
            "━━━━━━━━━━━━━━━━━━━━\n" +
            "💬 종합 평가\n" +
            "━━━━━━━━━━━━━━━━━━━━\n" +
            "%s\n\n" +
            "'다시'라고 입력하시면 새로운 검색을 시작할 수 있어요!",
            result.answeredCount,
            result.totalQuestions,
            result.totalScore,
            result.averageScore,
            result.passRate,
            result.overallFeedback
        );
    }

    private Integer extractJobNumber(String message) {
        String normalized = message.replaceAll("[^0-9]", "");
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<String> parseTechStacks(String message) {
        return java.util.Arrays.stream(message.split("[,，]"))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(java.util.stream.Collectors.toList());
    }

    private String getCategoryName(QuestionCategory category) {
        switch (category) {
            case TECHNICAL: return "기술";
            case EXPERIENCE: return "경험";
            case PROBLEM_SOLVING: return "문제해결";
            case CULTURE_FIT: return "문화적합성";
            case PROJECT: return "프로젝트";
            default: return "일반";
        }
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
