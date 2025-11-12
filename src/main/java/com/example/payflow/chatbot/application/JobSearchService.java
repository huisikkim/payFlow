package com.example.payflow.chatbot.application;

import com.example.payflow.chatbot.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobSearchService {

    private final JobRepository jobRepository;
    private final ConversationContextRepository contextRepository;

    @Transactional
    public ConversationContext getOrCreateContext(Long conversationId) {
        return contextRepository.findByConversationId(conversationId)
            .orElseGet(() -> {
                ConversationContext context = new ConversationContext(conversationId);
                return contextRepository.save(context);
            });
    }

    @Transactional
    public void updateRegion(Long conversationId, String region) {
        ConversationContext context = getOrCreateContext(conversationId);
        context.setSelectedRegion(region);
        context.moveToStep(ConversationStep.ASKING_INDUSTRY);
        contextRepository.save(context);
    }

    @Transactional
    public void updateIndustry(Long conversationId, String industry) {
        ConversationContext context = getOrCreateContext(conversationId);
        context.setSelectedIndustry(industry);
        context.moveToStep(ConversationStep.ASKING_SALARY);
        contextRepository.save(context);
    }

    @Transactional
    public void updateSalary(Long conversationId, Long minSalary, Long maxSalary) {
        ConversationContext context = getOrCreateContext(conversationId);
        context.setSalaryRange(minSalary, maxSalary);
        context.moveToStep(ConversationStep.SHOWING_RESULTS);
        contextRepository.save(context);
    }

    @Transactional
    public void resetContext(Long conversationId) {
        ConversationContext context = getOrCreateContext(conversationId);
        context.reset();
        contextRepository.save(context);
    }

    public List<String> getAvailableRegions() {
        return jobRepository.findDistinctRegions();
    }

    public List<String> getAvailableIndustries() {
        return jobRepository.findDistinctIndustries();
    }

    public List<Job> searchJobs(ConversationContext context) {
        return jobRepository.searchJobs(
            context.getSelectedRegion(),
            context.getSelectedIndustry(),
            context.getMinSalary(),
            context.getMaxSalary()
        );
    }

    public String extractRegion(String message) {
        List<String> regions = List.of("서울", "경기", "인천", "부산", "대구", "광주", "대전", 
                                       "울산", "세종", "강원", "충북", "충남", "전북", "전남", 
                                       "경북", "경남", "제주");
        
        for (String region : regions) {
            if (message.contains(region)) {
                return region;
            }
        }
        return null;
    }

    public String extractIndustry(String message) {
        List<String> industries = List.of("IT", "금융", "제조", "유통", "서비스", 
                                          "교육", "의료", "건설", "미디어", "게임", "스타트업");
        
        String normalized = message.toUpperCase();
        for (String industry : industries) {
            if (normalized.contains(industry.toUpperCase())) {
                return industry;
            }
        }
        return null;
    }

    public Long[] extractSalaryRange(String message) {
        // 패턴: "3000만원", "3000", "3000~5000", "3000만원~5000만원" 등
        Pattern pattern = Pattern.compile("(\\d+)(?:만원)?(?:\\s*~\\s*|\\s*-\\s*|\\s+)(\\d+)(?:만원)?");
        Matcher matcher = pattern.matcher(message);
        
        if (matcher.find()) {
            Long min = Long.parseLong(matcher.group(1)) * 10000;
            Long max = Long.parseLong(matcher.group(2)) * 10000;
            return new Long[]{min, max};
        }
        
        // 단일 값인 경우
        Pattern singlePattern = Pattern.compile("(\\d+)(?:만원)?");
        Matcher singleMatcher = singlePattern.matcher(message);
        if (singleMatcher.find()) {
            Long salary = Long.parseLong(singleMatcher.group(1)) * 10000;
            return new Long[]{salary, salary + 20000000}; // +2000만원 범위
        }
        
        return null;
    }
}
