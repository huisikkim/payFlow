package com.example.payflow.specification.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 1단계: OCR 단어 정규화
 * 문자열 정제를 통한 품목명 표준화
 */
@Slf4j
@Component
public class IngredientNormalizer {
    
    // 불용어 목록
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "국산", "수입산", "냉장", "냉동", "신선", "특상", "상품", "등급",
        "A급", "B급", "프리미엄", "고급", "일반", "대", "중", "소"
    ));
    
    // 괄호 안 내용 제거 패턴
    private static final Pattern PARENTHESES_PATTERN = Pattern.compile("\\([^)]*\\)|\\[[^]]*\\]|\\{[^}]*\\}");
    
    // 숫자+단위 패턴 (예: 3kg, 500g, 10개, 8단)
    private static final Pattern NUMBER_UNIT_PATTERN = Pattern.compile("\\d+(\\.\\d+)?(kg|g|ml|l|개|봉|팩|box|단)?", Pattern.CASE_INSENSITIVE);
    
    // 특수문자 패턴
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[^가-힣a-zA-Z0-9]");
    
    /**
     * OCR 텍스트 정규화
     * 예) "양 파 (국산) 3kg" -> "양파"
     */
    public String normalize(String ocrText) {
        if (ocrText == null || ocrText.trim().isEmpty()) {
            return "";
        }
        
        String normalized = ocrText;
        
        // 1. 소문자 변환
        normalized = normalized.toLowerCase();
        
        // 2. 괄호 안 내용 제거
        normalized = PARENTHESES_PATTERN.matcher(normalized).replaceAll("");
        
        // 3. 숫자+단위 제거 (예: 3kg, 500g)
        normalized = NUMBER_UNIT_PATTERN.matcher(normalized).replaceAll("");
        
        // 4. 공백 제거
        normalized = normalized.replaceAll("\\s+", "");
        
        // 5. 특수문자 제거
        normalized = SPECIAL_CHARS_PATTERN.matcher(normalized).replaceAll("");
        
        // 6. 불용어 제거
        for (String stopWord : STOP_WORDS) {
            normalized = normalized.replace(stopWord.toLowerCase(), "");
        }
        
        // 7. 최종 공백 정리
        normalized = normalized.trim();
        
        log.debug("Normalized: '{}' -> '{}'", ocrText, normalized);
        
        return normalized;
    }
    
    /**
     * 여러 OCR 텍스트를 한번에 정규화
     */
    public Set<String> normalizeAll(Set<String> ocrTexts) {
        Set<String> normalized = new HashSet<>();
        for (String text : ocrTexts) {
            String norm = normalize(text);
            if (!norm.isEmpty()) {
                normalized.add(norm);
            }
        }
        return normalized;
    }
}
