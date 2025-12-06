package com.example.payflow.youtube.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 제목 분석기
 */
@Slf4j
@Component
public class TitleAnalyzer {
    
    private static final int OPTIMAL_MIN_LENGTH = 40;
    private static final int OPTIMAL_MAX_LENGTH = 70;
    
    private static final List<String> EMOTIONAL_WORDS_KR = Arrays.asList(
        "완벽", "최고", "놀라운", "충격", "대박", "꿀팁", "필수", "강추", "추천",
        "실화", "레전드", "역대급", "신박", "혁신", "비밀", "진짜", "꼭", "반드시"
    );
    
    private static final List<String> EMOTIONAL_WORDS_EN = Arrays.asList(
        "amazing", "perfect", "best", "ultimate", "shocking", "must", "essential",
        "incredible", "awesome", "fantastic", "revolutionary", "secret", "proven"
    );
    
    /**
     * 제목 종합 분석
     */
    public TitleAnalysis analyze(String title, List<YouTubeVideo> competitors) {
        if (title == null || title.isEmpty()) {
            return TitleAnalysis.builder()
                .title("")
                .length(0)
                .score(0)
                .suggestions(Arrays.asList("제목이 없습니다."))
                .build();
        }
        
        int length = title.length();
        int wordCount = countWords(title);
        boolean hasNumbers = containsNumbers(title);
        boolean hasEmotionalWords = containsEmotionalWords(title);
        boolean hasQuestionMark = title.contains("?") || title.contains("？");
        boolean isOptimalLength = length >= OPTIMAL_MIN_LENGTH && length <= OPTIMAL_MAX_LENGTH;
        
        List<String> keywords = extractKeywords(title);
        
        // 점수 계산
        int score = calculateTitleScore(length, hasNumbers, hasEmotionalWords, 
                                       hasQuestionMark, keywords.size());
        
        // 개선 제안
        List<String> suggestions = generateSuggestions(title, length, hasNumbers, 
                                                       hasEmotionalWords, hasQuestionMark,
                                                       competitors);
        
        return TitleAnalysis.builder()
            .title(title)
            .length(length)
            .optimalLength(OPTIMAL_MAX_LENGTH)
            .isOptimalLength(isOptimalLength)
            .wordCount(wordCount)
            .hasNumbers(hasNumbers)
            .hasEmotionalWords(hasEmotionalWords)
            .hasQuestionMark(hasQuestionMark)
            .keywords(keywords)
            .score(score)
            .suggestions(suggestions)
            .build();
    }
    
    /**
     * 단어 수 계산
     */
    private int countWords(String title) {
        // 공백 기준 분리
        String[] words = title.trim().split("\\s+");
        return words.length;
    }
    
    /**
     * 숫자 포함 여부
     */
    private boolean containsNumbers(String title) {
        return Pattern.compile("\\d+").matcher(title).find();
    }
    
    /**
     * 감정 단어 포함 여부
     */
    private boolean containsEmotionalWords(String title) {
        String lowerTitle = title.toLowerCase();
        
        for (String word : EMOTIONAL_WORDS_KR) {
            if (title.contains(word)) return true;
        }
        
        for (String word : EMOTIONAL_WORDS_EN) {
            if (lowerTitle.contains(word)) return true;
        }
        
        return false;
    }
    
    /**
     * 키워드 추출
     */
    private List<String> extractKeywords(String title) {
        String[] words = title.split("\\s+");
        List<String> stopWords = Arrays.asList(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
            "of", "with", "by", "from", "up", "about", "into", "through",
            "이", "그", "저", "것", "수", "등", "및", "를", "을", "가", "에", "의", "와", "과"
        );
        
        return Arrays.stream(words)
            .map(String::toLowerCase)
            .filter(word -> word.length() > 2)
            .filter(word -> !stopWords.contains(word))
            .limit(5)
            .collect(Collectors.toList());
    }
    
    /**
     * 제목 점수 계산 (0-100)
     */
    private int calculateTitleScore(int length, boolean hasNumbers, 
                                    boolean hasEmotionalWords, boolean hasQuestionMark,
                                    int keywordCount) {
        int score = 0;
        
        // 1. 길이 점수 (30점)
        if (length >= OPTIMAL_MIN_LENGTH && length <= OPTIMAL_MAX_LENGTH) {
            score += 30;
        } else if (length >= 30 && length <= 80) {
            score += 20;
        } else if (length >= 20 && length <= 100) {
            score += 10;
        }
        
        // 2. 숫자 포함 (20점)
        if (hasNumbers) score += 20;
        
        // 3. 감정 단어 (20점)
        if (hasEmotionalWords) score += 20;
        
        // 4. 질문형 (15점)
        if (hasQuestionMark) score += 15;
        
        // 5. 키워드 다양성 (15점)
        score += Math.min(15, keywordCount * 3);
        
        return Math.min(100, score);
    }
    
    /**
     * 개선 제안 생성
     */
    private List<String> generateSuggestions(String title, int length, 
                                             boolean hasNumbers, boolean hasEmotionalWords,
                                             boolean hasQuestionMark,
                                             List<YouTubeVideo> competitors) {
        List<String> suggestions = new ArrayList<>();
        
        // 길이 관련
        if (length < OPTIMAL_MIN_LENGTH) {
            suggestions.add("제목이 너무 짧습니다. " + OPTIMAL_MIN_LENGTH + "자 이상을 권장합니다.");
        } else if (length > OPTIMAL_MAX_LENGTH) {
            suggestions.add("제목이 너무 깁니다. " + OPTIMAL_MAX_LENGTH + "자 이하를 권장합니다.");
        }
        
        // 숫자 관련
        if (!hasNumbers) {
            suggestions.add("숫자를 추가하면 클릭률이 높아집니다. (예: '5가지 방법', '2024년')");
        }
        
        // 감정 단어 관련
        if (!hasEmotionalWords) {
            suggestions.add("감정을 자극하는 단어를 추가하세요. (예: '완벽한', '놀라운', '필수')");
        }
        
        // 질문형 관련
        if (!hasQuestionMark && !title.contains("방법") && !title.contains("how")) {
            suggestions.add("질문형 제목은 호기심을 유발합니다. (예: '~하는 방법은?')");
        }
        
        // 경쟁 영상 분석 기반 제안
        if (competitors != null && !competitors.isEmpty()) {
            List<YouTubeVideo> topVideos = competitors.stream()
                .sorted((v1, v2) -> Long.compare(
                    v2.getViewCount() != null ? v2.getViewCount() : 0,
                    v1.getViewCount() != null ? v1.getViewCount() : 0
                ))
                .limit(3)
                .collect(Collectors.toList());
            
            // 상위 영상들의 평균 제목 길이
            double avgLength = topVideos.stream()
                .mapToInt(v -> v.getTitle() != null ? v.getTitle().length() : 0)
                .average()
                .orElse(0);
            
            if (avgLength > 0) {
                suggestions.add(String.format("상위 경쟁 영상의 평균 제목 길이는 %.0f자입니다.", avgLength));
            }
        }
        
        return suggestions;
    }
}
