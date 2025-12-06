package com.example.payflow.youtube.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * SEO 분석 결과
 */
@Getter
@Builder
public class SeoAnalysis {
    // 태그 분석
    private List<String> currentTags;
    private Integer tagCount;
    private List<String> recommendedTags;
    private List<String> missingTags;
    private List<String> duplicateTags;
    private Integer tagDiversityScore;  // 0-100
    
    // 설명문 분석
    private Integer descriptionLength;
    private Integer optimalDescriptionLength;  // 최소 250자 권장
    private Boolean hasLinks;
    private Boolean hasTimestamps;
    private Boolean hasHashtags;
    private Integer hashtagCount;
    private List<String> hashtags;
    private Integer descriptionScore;  // 0-100
    
    // 설명문 최적화 체크리스트
    private List<ChecklistItem> descriptionChecklist;
    
    // 전체 SEO 점수
    private Integer overallSeoScore;  // 0-100
    
    @Getter
    @Builder
    public static class ChecklistItem {
        private String item;
        private Boolean checked;
        private String recommendation;
    }
}
