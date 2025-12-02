package com.example.payflow.specification.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 재료 매칭 결과 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientMatchResult {
    
    private String originalText;      // 원본 OCR 텍스트
    private String normalizedText;    // 정규화된 텍스트
    private String standardName;      // 매칭된 표준 재료명
    private Double similarityScore;   // 유사도 점수 (0.0 ~ 1.0)
    private MatchType matchType;      // 매칭 타입
    private Boolean matched;          // 매칭 성공 여부
    private String failureReason;     // 실패 사유
    
    public enum MatchType {
        EXACT_MATCH,        // 동의어 사전 정확 매칭
        SIMILARITY_MATCH,   // 유사도 기반 매칭
        NO_MATCH           // 매칭 실패
    }
    
    /**
     * 동의어 사전 정확 매칭
     */
    public static IngredientMatchResult exactMatch(
            String originalText, 
            String normalizedText, 
            String standardName, 
            Double score) {
        return IngredientMatchResult.builder()
            .originalText(originalText)
            .normalizedText(normalizedText)
            .standardName(standardName)
            .similarityScore(score)
            .matchType(MatchType.EXACT_MATCH)
            .matched(true)
            .build();
    }
    
    /**
     * 유사도 기반 매칭
     */
    public static IngredientMatchResult similarityMatch(
            String originalText, 
            String normalizedText, 
            String standardName, 
            Double score) {
        return IngredientMatchResult.builder()
            .originalText(originalText)
            .normalizedText(normalizedText)
            .standardName(standardName)
            .similarityScore(score)
            .matchType(MatchType.SIMILARITY_MATCH)
            .matched(true)
            .build();
    }
    
    /**
     * 매칭 실패
     */
    public static IngredientMatchResult noMatch(String originalText, String reason) {
        return IngredientMatchResult.builder()
            .originalText(originalText)
            .normalizedText("")
            .standardName(null)
            .similarityScore(0.0)
            .matchType(MatchType.NO_MATCH)
            .matched(false)
            .failureReason(reason)
            .build();
    }
}
