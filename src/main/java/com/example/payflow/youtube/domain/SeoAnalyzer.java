package com.example.payflow.youtube.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * SEO 분석기
 */
@Slf4j
@Component
public class SeoAnalyzer {
    
    private static final int OPTIMAL_DESCRIPTION_LENGTH = 250;
    
    /**
     * SEO 종합 분석
     */
    public SeoAnalysis analyze(YouTubeVideo video, List<YouTubeVideo> competitors) {
        String description = video.getDescription() != null ? video.getDescription() : "";
        
        // 태그 분석 (YouTube API v3에서는 제한적이므로 설명문에서 추출)
        List<String> currentTags = extractTagsFromDescription(description);
        List<String> hashtags = extractHashtags(description);
        
        // 경쟁 영상의 태그 분석
        List<String> competitorTags = extractCompetitorTags(competitors);
        List<String> recommendedTags = recommendTags(currentTags, competitorTags);
        List<String> missingTags = findMissingTags(currentTags, competitorTags);
        List<String> duplicateTags = findDuplicateTags(currentTags);
        
        // 태그 다양성 점수
        int tagDiversityScore = calculateTagDiversityScore(currentTags, competitorTags);
        
        // 설명문 분석
        int descriptionLength = description.length();
        boolean hasLinks = containsLinks(description);
        boolean hasTimestamps = containsTimestamps(description);
        boolean hasHashtags = !hashtags.isEmpty();
        
        // 설명문 점수
        int descriptionScore = calculateDescriptionScore(descriptionLength, hasLinks, 
                                                         hasTimestamps, hasHashtags);
        
        // 설명문 체크리스트
        List<SeoAnalysis.ChecklistItem> checklist = generateDescriptionChecklist(
            descriptionLength, hasLinks, hasTimestamps, hasHashtags, currentTags.size()
        );
        
        // 전체 SEO 점수
        int overallSeoScore = (tagDiversityScore + descriptionScore) / 2;
        
        return SeoAnalysis.builder()
            .currentTags(currentTags)
            .tagCount(currentTags.size())
            .recommendedTags(recommendedTags)
            .missingTags(missingTags)
            .duplicateTags(duplicateTags)
            .tagDiversityScore(tagDiversityScore)
            .descriptionLength(descriptionLength)
            .optimalDescriptionLength(OPTIMAL_DESCRIPTION_LENGTH)
            .hasLinks(hasLinks)
            .hasTimestamps(hasTimestamps)
            .hasHashtags(hasHashtags)
            .hashtagCount(hashtags.size())
            .hashtags(hashtags)
            .descriptionScore(descriptionScore)
            .descriptionChecklist(checklist)
            .overallSeoScore(overallSeoScore)
            .build();
    }
    
    /**
     * 설명문에서 태그 추출 (키워드 기반)
     */
    private List<String> extractTagsFromDescription(String description) {
        if (description == null || description.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 해시태그 추출
        List<String> tags = new ArrayList<>();
        Pattern pattern = Pattern.compile("#([a-zA-Z0-9가-힣_]+)");
        Matcher matcher = pattern.matcher(description);
        
        while (matcher.find()) {
            tags.add(matcher.group(1).toLowerCase());
        }
        
        return tags.stream().distinct().collect(Collectors.toList());
    }
    
    /**
     * 해시태그 추출
     */
    private List<String> extractHashtags(String description) {
        List<String> hashtags = new ArrayList<>();
        if (description == null) return hashtags;
        
        Pattern pattern = Pattern.compile("#([a-zA-Z0-9가-힣_]+)");
        Matcher matcher = pattern.matcher(description);
        
        while (matcher.find()) {
            hashtags.add("#" + matcher.group(1));
        }
        
        return hashtags;
    }
    
    /**
     * 경쟁 영상의 태그 수집
     */
    private List<String> extractCompetitorTags(List<YouTubeVideo> competitors) {
        Map<String, Integer> tagFrequency = new HashMap<>();
        
        for (YouTubeVideo competitor : competitors) {
            List<String> tags = extractTagsFromDescription(
                competitor.getDescription() != null ? competitor.getDescription() : ""
            );
            
            for (String tag : tags) {
                tagFrequency.put(tag, tagFrequency.getOrDefault(tag, 0) + 1);
            }
        }
        
        // 빈도순 정렬
        return tagFrequency.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .map(Map.Entry::getKey)
            .limit(20)
            .collect(Collectors.toList());
    }
    
    /**
     * 추천 태그 생성
     */
    private List<String> recommendTags(List<String> currentTags, List<String> competitorTags) {
        return competitorTags.stream()
            .filter(tag -> !currentTags.contains(tag))
            .limit(10)
            .collect(Collectors.toList());
    }
    
    /**
     * 누락된 인기 태그 찾기
     */
    private List<String> findMissingTags(List<String> currentTags, List<String> competitorTags) {
        return competitorTags.stream()
            .limit(5)  // 상위 5개만
            .filter(tag -> !currentTags.contains(tag))
            .collect(Collectors.toList());
    }
    
    /**
     * 중복 태그 찾기
     */
    private List<String> findDuplicateTags(List<String> tags) {
        Set<String> seen = new HashSet<>();
        return tags.stream()
            .filter(tag -> !seen.add(tag.toLowerCase()))
            .collect(Collectors.toList());
    }
    
    /**
     * 태그 다양성 점수 계산
     */
    private int calculateTagDiversityScore(List<String> currentTags, List<String> competitorTags) {
        int score = 0;
        
        // 1. 태그 개수 (40점)
        int tagCount = currentTags.size();
        if (tagCount >= 10) score += 40;
        else if (tagCount >= 5) score += 30;
        else if (tagCount >= 3) score += 20;
        else if (tagCount >= 1) score += 10;
        
        // 2. 인기 태그 포함 여부 (40점)
        if (!competitorTags.isEmpty()) {
            List<String> topTags = competitorTags.stream().limit(5).collect(Collectors.toList());
            long matchCount = currentTags.stream()
                .filter(topTags::contains)
                .count();
            score += (int) (matchCount * 8);  // 최대 40점
        }
        
        // 3. 태그 중복 없음 (20점)
        if (findDuplicateTags(currentTags).isEmpty()) {
            score += 20;
        }
        
        return Math.min(100, score);
    }
    
    /**
     * 링크 포함 여부
     */
    private boolean containsLinks(String description) {
        Pattern pattern = Pattern.compile("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=]+");
        return pattern.matcher(description).find();
    }
    
    /**
     * 타임스탬프 포함 여부
     */
    private boolean containsTimestamps(String description) {
        // 00:00, 0:00, 1:23:45 형식
        Pattern pattern = Pattern.compile("\\d{1,2}:\\d{2}(:\\d{2})?");
        return pattern.matcher(description).find();
    }
    
    /**
     * 설명문 점수 계산
     */
    private int calculateDescriptionScore(int length, boolean hasLinks, 
                                          boolean hasTimestamps, boolean hasHashtags) {
        int score = 0;
        
        // 1. 길이 (40점)
        if (length >= OPTIMAL_DESCRIPTION_LENGTH) score += 40;
        else if (length >= 150) score += 30;
        else if (length >= 100) score += 20;
        else if (length >= 50) score += 10;
        
        // 2. 링크 포함 (20점)
        if (hasLinks) score += 20;
        
        // 3. 타임스탬프 (20점)
        if (hasTimestamps) score += 20;
        
        // 4. 해시태그 (20점)
        if (hasHashtags) score += 20;
        
        return Math.min(100, score);
    }
    
    /**
     * 설명문 최적화 체크리스트 생성
     */
    private List<SeoAnalysis.ChecklistItem> generateDescriptionChecklist(
            int length, boolean hasLinks, boolean hasTimestamps, 
            boolean hasHashtags, int tagCount) {
        
        List<SeoAnalysis.ChecklistItem> checklist = new ArrayList<>();
        
        checklist.add(SeoAnalysis.ChecklistItem.builder()
            .item("설명문 길이 250자 이상")
            .checked(length >= OPTIMAL_DESCRIPTION_LENGTH)
            .recommendation(length < OPTIMAL_DESCRIPTION_LENGTH ? 
                "현재 " + length + "자입니다. 최소 250자 이상 작성하세요." : 
                "적절한 길이입니다.")
            .build());
        
        checklist.add(SeoAnalysis.ChecklistItem.builder()
            .item("관련 링크 포함 (SNS, 웹사이트 등)")
            .checked(hasLinks)
            .recommendation(hasLinks ? 
                "링크가 포함되어 있습니다." : 
                "관련 링크를 추가하여 시청자 참여를 유도하세요.")
            .build());
        
        checklist.add(SeoAnalysis.ChecklistItem.builder()
            .item("타임스탬프 추가")
            .checked(hasTimestamps)
            .recommendation(hasTimestamps ? 
                "타임스탬프가 있습니다." : 
                "주요 구간에 타임스탬프를 추가하면 시청 경험이 향상됩니다.")
            .build());
        
        checklist.add(SeoAnalysis.ChecklistItem.builder()
            .item("해시태그 사용")
            .checked(hasHashtags)
            .recommendation(hasHashtags ? 
                "해시태그를 사용하고 있습니다." : 
                "관련 해시태그를 추가하여 검색 노출을 높이세요.")
            .build());
        
        checklist.add(SeoAnalysis.ChecklistItem.builder()
            .item("태그 5개 이상")
            .checked(tagCount >= 5)
            .recommendation(tagCount >= 5 ? 
                "충분한 태그를 사용하고 있습니다." : 
                "현재 " + tagCount + "개입니다. 최소 5개 이상의 태그를 추가하세요.")
            .build());
        
        return checklist;
    }
}
