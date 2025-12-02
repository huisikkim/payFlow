package com.example.payflow.specification.infrastructure;

import org.springframework.stereotype.Component;

/**
 * Jaro-Winkler Distance 알고리즘 구현
 * 문자열 유사도 계산 (0.0 ~ 1.0)
 */
@Component
public class JaroWinklerMatcher {
    
    private static final double SCALING_FACTOR = 0.1;
    private static final int PREFIX_LENGTH = 4;
    
    /**
     * Jaro-Winkler 유사도 계산
     * @param s1 첫 번째 문자열
     * @param s2 두 번째 문자열
     * @return 유사도 (0.0 ~ 1.0)
     */
    public double similarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }
        
        if (s1.equals(s2)) {
            return 1.0;
        }
        
        // Jaro 유사도 계산
        double jaroSimilarity = jaroSimilarity(s1, s2);
        
        // 공통 접두사 길이 계산
        int prefixLength = commonPrefixLength(s1, s2);
        
        // Jaro-Winkler 유사도 = Jaro + (prefix * scaling * (1 - Jaro))
        return jaroSimilarity + (prefixLength * SCALING_FACTOR * (1.0 - jaroSimilarity));
    }
    
    /**
     * Jaro 유사도 계산
     */
    private double jaroSimilarity(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        
        if (len1 == 0 && len2 == 0) {
            return 1.0;
        }
        
        if (len1 == 0 || len2 == 0) {
            return 0.0;
        }
        
        // 매칭 윈도우 크기
        int matchWindow = Math.max(len1, len2) / 2 - 1;
        if (matchWindow < 1) {
            matchWindow = 1;
        }
        
        boolean[] s1Matches = new boolean[len1];
        boolean[] s2Matches = new boolean[len2];
        
        int matches = 0;
        int transpositions = 0;
        
        // 매칭 찾기
        for (int i = 0; i < len1; i++) {
            int start = Math.max(0, i - matchWindow);
            int end = Math.min(i + matchWindow + 1, len2);
            
            for (int j = start; j < end; j++) {
                if (s2Matches[j] || s1.charAt(i) != s2.charAt(j)) {
                    continue;
                }
                s1Matches[i] = true;
                s2Matches[j] = true;
                matches++;
                break;
            }
        }
        
        if (matches == 0) {
            return 0.0;
        }
        
        // 전치(transposition) 계산
        int k = 0;
        for (int i = 0; i < len1; i++) {
            if (!s1Matches[i]) {
                continue;
            }
            while (!s2Matches[k]) {
                k++;
            }
            if (s1.charAt(i) != s2.charAt(k)) {
                transpositions++;
            }
            k++;
        }
        
        // Jaro 유사도 공식
        return (matches / (double) len1 + 
                matches / (double) len2 + 
                (matches - transpositions / 2.0) / matches) / 3.0;
    }
    
    /**
     * 공통 접두사 길이 계산 (최대 PREFIX_LENGTH)
     */
    private int commonPrefixLength(String s1, String s2) {
        int minLen = Math.min(Math.min(s1.length(), s2.length()), PREFIX_LENGTH);
        
        for (int i = 0; i < minLen; i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                return i;
            }
        }
        
        return minLen;
    }
}
