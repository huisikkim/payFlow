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
        "A급", "B급", "프리미엄", "고급", "일반", "대", "중", "소",
        "묶음", "박스", "케이스", "병", "캔", "팩"
    ));
    
    // 테이블 헤더 키워드 (매칭 대상에서 완전 제외)
    private static final Set<String> TABLE_HEADERS = new HashSet<>(Arrays.asList(
        "no", "no.", "상품명", "품명", "품목", "품목명", "제품명", "메뉴명",
        "단가", "단위", "가격", "금액", "합계", "총액", "부가세", "vat",
        "수량", "qty", "quantity", "ea", "개수",
        "비고", "규격", "단위", "코드", "바코드", "품번",
        "공급가", "공급가액", "세액", "합계금액", "총합계",
        "특매할인", "할인", "할인금액", "명"
    ));
    
    // 행번호 패턴 (예: D01, 001, 01, )01, $, 8 등 앞에 붙는 번호/기호)
    private static final Pattern ROW_NUMBER_PREFIX_PATTERN = Pattern.compile("^[D$)\\d]{0,3}\\d{1,3}(?=[가-힣a-zA-Z])");
    
    // 괄호 안 내용 제거 패턴
    private static final Pattern PARENTHESES_PATTERN = Pattern.compile("\\([^)]*\\)|\\[[^]]*\\]|\\{[^}]*\\}");
    
    // 숫자+단위 패턴 (예: 3kg, 500g, 10개, 8단, 500ml, 750ml, 360ml)
    private static final Pattern NUMBER_UNIT_PATTERN = Pattern.compile("\\d+(\\.\\d+)?(kg|g|ml|l|개|봉|팩|box|단)?", Pattern.CASE_INSENSITIVE);
    
    // 바코드 패턴 (13자리 숫자)
    private static final Pattern BARCODE_PATTERN = Pattern.compile("\\d{13}");
    
    // 가격 패턴 (쉼표 포함 숫자, 예: 2,480 또는 -130)
    private static final Pattern PRICE_PATTERN = Pattern.compile("-?\\d{1,3}(,\\d{3})*");
    
    // 특수문자 패턴 (한글, 영문만 남김)
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[^가-힣a-zA-Z]");
    
    // 단독 영문자 패턴 (m, l 등 단위 잔여물)
    private static final Pattern SINGLE_LETTER_PATTERN = Pattern.compile("(?<=[가-힣])[a-z]$|^[a-z](?=[가-힣])");
    
    /**
     * OCR 텍스트 정규화
     * 예) "D01지리산샘물 500mL.묶음(20)" -> "지리산샘물"
     * 예) ")02처음처럼(병)360m|" -> "처음처럼"
     */
    public String normalize(String ocrText) {
        if (ocrText == null || ocrText.trim().isEmpty()) {
            return "";
        }
        
        String normalized = ocrText;
        
        // 0-1. 테이블 헤더 키워드 체크 (매칭 대상에서 완전 제외)
        String lowerText = normalized.toLowerCase().replaceAll("[^가-힣a-z]", "").trim();
        if (TABLE_HEADERS.contains(lowerText)) {
            log.debug("테이블 헤더 제외: '{}'", ocrText);
            return "";
        }
        
        // 0-2. 바코드/가격 패턴 감지 - 숫자만 있으면 빈 문자열 반환
        if (BARCODE_PATTERN.matcher(normalized.replaceAll("[^0-9]", "")).matches()) {
            String withoutNumbers = normalized.replaceAll("[0-9,\\-\\s]", "").trim();
            if (withoutNumbers.isEmpty()) {
                return "";
            }
        }
        
        // 1. 행번호 접두사 제거 (D01, )02, $, 8 등)
        normalized = ROW_NUMBER_PREFIX_PATTERN.matcher(normalized).replaceAll("");
        
        // 2. 소문자 변환
        normalized = normalized.toLowerCase();
        
        // 3. 괄호 안 내용 제거
        normalized = PARENTHESES_PATTERN.matcher(normalized).replaceAll("");
        
        // 4. 가격 패턴 제거
        normalized = PRICE_PATTERN.matcher(normalized).replaceAll("");
        
        // 5. 숫자+단위 제거 (예: 3kg, 500ml)
        normalized = NUMBER_UNIT_PATTERN.matcher(normalized).replaceAll("");
        
        // 6. 공백 제거
        normalized = normalized.replaceAll("\\s+", "");
        
        // 7. 특수문자 제거 (|, ., / 등)
        normalized = SPECIAL_CHARS_PATTERN.matcher(normalized).replaceAll("");
        
        // 8. 단독 영문자 제거 (ml에서 숫자 제거 후 남은 m 등)
        normalized = SINGLE_LETTER_PATTERN.matcher(normalized).replaceAll("");
        
        // 9. 불용어 제거
        for (String stopWord : STOP_WORDS) {
            normalized = normalized.replace(stopWord.toLowerCase(), "");
        }
        
        // 10. 최종 공백 정리
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
