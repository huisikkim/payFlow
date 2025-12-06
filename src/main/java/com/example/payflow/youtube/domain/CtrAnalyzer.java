package com.example.payflow.youtube.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * CTR(Click-Through Rate) 분석기
 */
@Slf4j
@Component
public class CtrAnalyzer {
    
    /**
     * CTR 추정
     */
    public CtrEstimate estimate(YouTubeVideo video, TitleAnalysis titleAnalysis, 
                                SeoAnalysis seoAnalysis) {
        
        List<CtrEstimate.CtrFactor> factors = new ArrayList<>();
        int totalScore = 0;
        
        // 1. 제목 길이 (20점)
        int titleLengthScore = evaluateTitleLength(video.getTitle(), factors);
        totalScore += titleLengthScore;
        
        // 2. 제목에 숫자 포함 (15점)
        int numberScore = evaluateNumbers(video.getTitle(), factors);
        totalScore += numberScore;
        
        // 3. 감정 단어 (15점)
        int emotionalScore = evaluateEmotionalWords(titleAnalysis, factors);
        totalScore += emotionalScore;
        
        // 4. 해시태그 수 (10점)
        int hashtagScore = evaluateHashtags(seoAnalysis, factors);
        totalScore += hashtagScore;
        
        // 5. 썸네일 품질 (간접 평가 - 채널 구독자 기반) (15점)
        int thumbnailScore = evaluateThumbnailQuality(video, factors);
        totalScore += thumbnailScore;
        
        // 6. 채널 신뢰도 (구독자 수) (15점)
        int channelScore = evaluateChannelTrust(video, factors);
        totalScore += channelScore;
        
        // 7. 설명문 품질 (10점)
        int descriptionScore = evaluateDescription(seoAnalysis, factors);
        totalScore += descriptionScore;
        
        // CTR 추정 (%)
        double estimatedCtr = calculateCtr(totalScore);
        String ctrLevel = categorizeCtrLevel(estimatedCtr);
        
        // 개선 제안
        List<String> improvements = generateImprovements(factors, estimatedCtr);
        
        return CtrEstimate.builder()
            .estimatedCtr(estimatedCtr)
            .ctrLevel(ctrLevel)
            .factors(factors)
            .improvements(improvements)
            .build();
    }
    
    /**
     * 제목 길이 평가
     */
    private int evaluateTitleLength(String title, List<CtrEstimate.CtrFactor> factors) {
        int length = title != null ? title.length() : 0;
        int score;
        String impact;
        String description;
        
        if (length >= 40 && length <= 70) {
            score = 20;
            impact = "긍정적";
            description = "최적의 제목 길이입니다 (" + length + "자)";
        } else if (length >= 30 && length <= 80) {
            score = 15;
            impact = "중립";
            description = "적절한 제목 길이입니다 (" + length + "자)";
        } else if (length < 30) {
            score = 5;
            impact = "부정적";
            description = "제목이 너무 짧습니다 (" + length + "자)";
        } else {
            score = 10;
            impact = "부정적";
            description = "제목이 너무 깁니다 (" + length + "자)";
        }
        
        factors.add(CtrEstimate.CtrFactor.builder()
            .factor("제목 길이")
            .score(score)
            .impact(impact)
            .description(description)
            .build());
        
        return score;
    }
    
    /**
     * 숫자 포함 평가
     */
    private int evaluateNumbers(String title, List<CtrEstimate.CtrFactor> factors) {
        boolean hasNumbers = title != null && Pattern.compile("\\d+").matcher(title).find();
        
        int score = hasNumbers ? 15 : 0;
        String impact = hasNumbers ? "긍정적" : "부정적";
        String description = hasNumbers ? 
            "제목에 숫자가 포함되어 있습니다" : 
            "제목에 숫자를 추가하면 CTR이 향상됩니다";
        
        factors.add(CtrEstimate.CtrFactor.builder()
            .factor("숫자 포함")
            .score(score)
            .impact(impact)
            .description(description)
            .build());
        
        return score;
    }
    
    /**
     * 감정 단어 평가
     */
    private int evaluateEmotionalWords(TitleAnalysis titleAnalysis, 
                                       List<CtrEstimate.CtrFactor> factors) {
        boolean hasEmotional = titleAnalysis != null && 
                               titleAnalysis.getHasEmotionalWords() != null && 
                               titleAnalysis.getHasEmotionalWords();
        
        int score = hasEmotional ? 15 : 0;
        String impact = hasEmotional ? "긍정적" : "부정적";
        String description = hasEmotional ? 
            "감정을 자극하는 단어가 포함되어 있습니다" : 
            "감정 단어를 추가하면 클릭률이 높아집니다";
        
        factors.add(CtrEstimate.CtrFactor.builder()
            .factor("감정 단어")
            .score(score)
            .impact(impact)
            .description(description)
            .build());
        
        return score;
    }
    
    /**
     * 해시태그 평가
     */
    private int evaluateHashtags(SeoAnalysis seoAnalysis, List<CtrEstimate.CtrFactor> factors) {
        int hashtagCount = seoAnalysis != null && seoAnalysis.getHashtagCount() != null ? 
                          seoAnalysis.getHashtagCount() : 0;
        
        int score;
        String impact;
        String description;
        
        if (hashtagCount >= 3) {
            score = 10;
            impact = "긍정적";
            description = "적절한 해시태그를 사용하고 있습니다 (" + hashtagCount + "개)";
        } else if (hashtagCount >= 1) {
            score = 5;
            impact = "중립";
            description = "해시태그가 있지만 더 추가할 수 있습니다 (" + hashtagCount + "개)";
        } else {
            score = 0;
            impact = "부정적";
            description = "해시태그를 추가하여 검색 노출을 높이세요";
        }
        
        factors.add(CtrEstimate.CtrFactor.builder()
            .factor("해시태그 수")
            .score(score)
            .impact(impact)
            .description(description)
            .build());
        
        return score;
    }
    
    /**
     * 썸네일 품질 평가 (간접)
     */
    private int evaluateThumbnailQuality(YouTubeVideo video, List<CtrEstimate.CtrFactor> factors) {
        // 썸네일 직접 분석은 불가하므로, 채널의 평균 조회수로 간접 평가
        long views = video.getViewCount() != null ? video.getViewCount() : 0;
        Long subscribers = video.getChannelSubscriberCount();
        
        int score = 10;  // 기본 점수
        String impact = "중립";
        String description = "썸네일 품질을 직접 평가할 수 없습니다";
        
        // 조회수가 구독자 수보다 높으면 썸네일이 효과적
        if (subscribers != null && subscribers > 0 && views > subscribers) {
            score = 15;
            impact = "긍정적";
            description = "조회수가 구독자 수를 초과하여 썸네일이 효과적입니다";
        }
        
        factors.add(CtrEstimate.CtrFactor.builder()
            .factor("썸네일 품질 (간접)")
            .score(score)
            .impact(impact)
            .description(description)
            .build());
        
        return score;
    }
    
    /**
     * 채널 신뢰도 평가
     */
    private int evaluateChannelTrust(YouTubeVideo video, List<CtrEstimate.CtrFactor> factors) {
        Long subscribers = video.getChannelSubscriberCount();
        
        int score;
        String impact;
        String description;
        
        if (subscribers == null) {
            score = 5;
            impact = "중립";
            description = "채널 구독자 정보를 확인할 수 없습니다";
        } else if (subscribers >= 1000000) {
            score = 15;
            impact = "긍정적";
            description = "대형 채널로 높은 신뢰도를 가지고 있습니다";
        } else if (subscribers >= 100000) {
            score = 12;
            impact = "긍정적";
            description = "중견 채널로 적절한 신뢰도를 가지고 있습니다";
        } else if (subscribers >= 10000) {
            score = 8;
            impact = "중립";
            description = "성장 중인 채널입니다";
        } else {
            score = 5;
            impact = "중립";
            description = "소규모 채널로 신뢰도 구축이 필요합니다";
        }
        
        factors.add(CtrEstimate.CtrFactor.builder()
            .factor("채널 신뢰도")
            .score(score)
            .impact(impact)
            .description(description)
            .build());
        
        return score;
    }
    
    /**
     * 설명문 품질 평가
     */
    private int evaluateDescription(SeoAnalysis seoAnalysis, List<CtrEstimate.CtrFactor> factors) {
        int descScore = seoAnalysis != null && seoAnalysis.getDescriptionScore() != null ? 
                       seoAnalysis.getDescriptionScore() : 0;
        
        int score = (int) (descScore * 0.1);  // 최대 10점
        String impact = descScore >= 70 ? "긍정적" : descScore >= 40 ? "중립" : "부정적";
        String description = "설명문 품질 점수: " + descScore + "/100";
        
        factors.add(CtrEstimate.CtrFactor.builder()
            .factor("설명문 품질")
            .score(score)
            .impact(impact)
            .description(description)
            .build());
        
        return score;
    }
    
    /**
     * CTR 계산 (%)
     */
    private double calculateCtr(int totalScore) {
        // 100점 만점을 10% CTR로 변환 (일반적으로 YouTube CTR은 2-10%)
        double baseCtr = 2.0;  // 기본 2%
        double maxCtr = 10.0;  // 최대 10%
        
        double ctr = baseCtr + (totalScore / 100.0) * (maxCtr - baseCtr);
        return Math.round(ctr * 100.0) / 100.0;  // 소수점 2자리
    }
    
    /**
     * CTR 레벨 분류
     */
    private String categorizeCtrLevel(double ctr) {
        if (ctr >= 8.0) return "매우 높음";
        if (ctr >= 6.0) return "높음";
        if (ctr >= 4.0) return "보통";
        if (ctr >= 2.5) return "낮음";
        return "매우 낮음";
    }
    
    /**
     * 개선 제안 생성
     */
    private List<String> generateImprovements(List<CtrEstimate.CtrFactor> factors, double ctr) {
        List<String> improvements = new ArrayList<>();
        
        // 부정적 요소 개선
        for (CtrEstimate.CtrFactor factor : factors) {
            if ("부정적".equals(factor.getImpact())) {
                improvements.add(factor.getFactor() + ": " + factor.getDescription());
            }
        }
        
        // 전체 CTR 평가
        if (ctr < 4.0) {
            improvements.add("전체적인 CTR이 낮습니다. 제목과 썸네일을 개선하세요.");
        } else if (ctr >= 8.0) {
            improvements.add("매우 높은 CTR입니다! 현재 전략을 유지하세요.");
        }
        
        return improvements;
    }
}
