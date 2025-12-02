package com.example.payflow.specification.application;

import com.example.payflow.specification.domain.IngredientSynonym;
import com.example.payflow.specification.domain.IngredientSynonymRepository;
import com.example.payflow.specification.infrastructure.JaroWinklerMatcher;
import com.example.payflow.specification.presentation.dto.IngredientMatchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 규칙 기반 메뉴 매칭 서비스
 * 3단계 매칭 알고리즘:
 * 1. OCR 단어 정규화
 * 2. 사전(Map) 기반 동의어 매핑
 * 3. Jaro-Winkler 유사도 기반 자동 매칭
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RuleBasedMatchingService {
    
    private final IngredientNormalizer normalizer;
    private final IngredientSynonymRepository synonymRepository;
    private final JaroWinklerMatcher jaroWinklerMatcher;
    
    // 유사도 임계값 (0.7 이상이면 매칭 성공)
    private static final double SIMILARITY_THRESHOLD = 0.7;
    
    /**
     * OCR 텍스트를 표준 재료명으로 매칭
     * @param ocrText OCR로 추출된 텍스트
     * @return 매칭 결과
     */
    public IngredientMatchResult matchIngredient(String ocrText) {
        log.info("매칭 시작: '{}'", ocrText);
        
        // 1단계: 정규화
        String normalized = normalizer.normalize(ocrText);
        log.debug("1단계 정규화: '{}' -> '{}'", ocrText, normalized);
        
        if (normalized.isEmpty()) {
            return IngredientMatchResult.noMatch(ocrText, "정규화 후 빈 문자열");
        }
        
        // 2단계: 동의어 사전 매칭
        Optional<IngredientSynonym> synonymMatch = synonymRepository.findBySynonym(normalized);
        if (synonymMatch.isPresent()) {
            IngredientSynonym synonym = synonymMatch.get();
            log.info("2단계 동의어 매칭 성공: '{}' -> '{}'", normalized, synonym.getStandardName());
            return IngredientMatchResult.exactMatch(
                ocrText, 
                normalized, 
                synonym.getStandardName(), 
                synonym.getSimilarityScore()
            );
        }
        
        // 3단계: Jaro-Winkler 유사도 매칭
        List<String> standardNames = synonymRepository.findAllStandardNames();
        
        String bestMatch = null;
        double bestScore = 0.0;
        
        for (String standardName : standardNames) {
            double similarity = jaroWinklerMatcher.similarity(normalized, standardName);
            
            if (similarity > bestScore) {
                bestScore = similarity;
                bestMatch = standardName;
            }
        }
        
        if (bestScore >= SIMILARITY_THRESHOLD) {
            log.info("3단계 유사도 매칭 성공: '{}' -> '{}' (유사도: {})", 
                normalized, bestMatch, bestScore);
            return IngredientMatchResult.similarityMatch(
                ocrText, 
                normalized, 
                bestMatch, 
                bestScore
            );
        }
        
        log.warn("매칭 실패: '{}' (최고 유사도: {})", normalized, bestScore);
        return IngredientMatchResult.noMatch(ocrText, 
            String.format("유사도 부족 (최고: %.2f < %.2f)", bestScore, SIMILARITY_THRESHOLD));
    }
    
    /**
     * 여러 OCR 텍스트를 한번에 매칭
     */
    public List<IngredientMatchResult> matchIngredients(List<String> ocrTexts) {
        return ocrTexts.stream()
            .map(this::matchIngredient)
            .collect(Collectors.toList());
    }
    
    /**
     * 동의어 추가 (관리자 기능)
     */
    @Transactional
    public IngredientSynonym addSynonym(String standardName, String synonym, Double similarityScore) {
        // 중복 체크
        if (synonymRepository.existsBySynonym(synonym)) {
            throw new IllegalArgumentException("이미 등록된 동의어입니다: " + synonym);
        }
        
        IngredientSynonym ingredientSynonym = IngredientSynonym.builder()
            .standardName(standardName)
            .synonym(synonym)
            .similarityScore(similarityScore != null ? similarityScore : 1.0)
            .build();
        
        return synonymRepository.save(ingredientSynonym);
    }
    
    /**
     * 표준 재료명의 모든 동의어 조회
     */
    public List<IngredientSynonym> getSynonyms(String standardName) {
        return synonymRepository.findByStandardName(standardName);
    }
    
    /**
     * 모든 표준 재료명 목록
     */
    public List<String> getAllStandardNames() {
        return synonymRepository.findAllStandardNames();
    }
}
