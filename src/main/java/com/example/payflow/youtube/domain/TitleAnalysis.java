package com.example.payflow.youtube.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 제목 분석 결과
 */
@Getter
@Builder
public class TitleAnalysis {
    private String title;
    private Integer length;
    private Integer optimalLength;  // 최적 길이 (40-70자)
    private Boolean isOptimalLength;
    private Integer wordCount;
    private Boolean hasNumbers;
    private Boolean hasEmotionalWords;
    private Boolean hasQuestionMark;
    private List<String> keywords;
    private Integer score;  // 0-100
    private List<String> suggestions;
}
