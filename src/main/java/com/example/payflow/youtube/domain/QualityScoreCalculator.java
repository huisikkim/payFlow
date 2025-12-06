package com.example.payflow.youtube.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 영상 품질 세부 점수 계산기
 */
@Slf4j
@Component
public class QualityScoreCalculator {
    
    /**
     * 품질 점수 계산
     */
    public QualityScore calculate(TitleAnalysis titleAnalysis, 
                                  SeoAnalysis seoAnalysis,
                                  YouTubeVideo video) {
        
        // 1. 제목 최적화 점수
        int titleScore = titleAnalysis != null && titleAnalysis.getScore() != null ? 
                        titleAnalysis.getScore() : 0;
        QualityScore.ScoreDetail titleDetail = evaluateTitleScore(titleScore, titleAnalysis);
        
        // 2. 태그 다양성 점수
        int tagScore = seoAnalysis != null && seoAnalysis.getTagDiversityScore() != null ? 
                      seoAnalysis.getTagDiversityScore() : 0;
        QualityScore.ScoreDetail tagDetail = evaluateTagScore(tagScore, seoAnalysis);
        
        // 3. 설명문 길이 점수
        int descScore = seoAnalysis != null && seoAnalysis.getDescriptionScore() != null ? 
                       seoAnalysis.getDescriptionScore() : 0;
        QualityScore.ScoreDetail descDetail = evaluateDescriptionScore(descScore, seoAnalysis);
        
        // 4. 참여도 점수
        int engagementScore = calculateEngagementScore(video);
        QualityScore.ScoreDetail engagementDetail = evaluateEngagementScore(engagementScore, video);
        
        // 종합 점수 (가중 평균)
        int overallScore = (int) (
            titleScore * 0.3 +
            tagScore * 0.2 +
            descScore * 0.2 +
            engagementScore * 0.3
        );
        
        String grade = calculateGrade(overallScore);
        
        return QualityScore.builder()
            .titleOptimizationScore(titleScore)
            .tagDiversityScore(tagScore)
            .descriptionLengthScore(descScore)
            .engagementScore(engagementScore)
            .overallScore(overallScore)
            .grade(grade)
            .titleDetail(titleDetail)
            .tagDetail(tagDetail)
            .descriptionDetail(descDetail)
            .engagementDetail(engagementDetail)
            .build();
    }
    
    /**
     * 제목 점수 평가
     */
    private QualityScore.ScoreDetail evaluateTitleScore(int score, TitleAnalysis analysis) {
        String status;
        String feedback;
        
        if (score >= 80) {
            status = "우수";
            feedback = "제목이 매우 잘 최적화되어 있습니다.";
        } else if (score >= 60) {
            status = "양호";
            feedback = "제목이 적절하지만 개선의 여지가 있습니다.";
        } else {
            status = "개선 필요";
            feedback = "제목을 개선하여 클릭률을 높이세요.";
            if (analysis != null && analysis.getSuggestions() != null && !analysis.getSuggestions().isEmpty()) {
                feedback += " " + analysis.getSuggestions().get(0);
            }
        }
        
        return QualityScore.ScoreDetail.builder()
            .score(score)
            .status(status)
            .feedback(feedback)
            .build();
    }
    
    /**
     * 태그 점수 평가
     */
    private QualityScore.ScoreDetail evaluateTagScore(int score, SeoAnalysis analysis) {
        String status;
        String feedback;
        
        if (score >= 80) {
            status = "우수";
            feedback = "태그가 다양하고 적절하게 사용되고 있습니다.";
        } else if (score >= 60) {
            status = "양호";
            feedback = "태그를 더 추가하면 검색 노출이 향상됩니다.";
        } else {
            status = "개선 필요";
            feedback = "태그를 추가하여 SEO를 개선하세요.";
            if (analysis != null && analysis.getRecommendedTags() != null && !analysis.getRecommendedTags().isEmpty()) {
                feedback += " 추천 태그: " + String.join(", ", analysis.getRecommendedTags().subList(0, Math.min(3, analysis.getRecommendedTags().size())));
            }
        }
        
        return QualityScore.ScoreDetail.builder()
            .score(score)
            .status(status)
            .feedback(feedback)
            .build();
    }
    
    /**
     * 설명문 점수 평가
     */
    private QualityScore.ScoreDetail evaluateDescriptionScore(int score, SeoAnalysis analysis) {
        String status;
        String feedback;
        
        if (score >= 80) {
            status = "우수";
            feedback = "설명문이 충분히 상세하고 최적화되어 있습니다.";
        } else if (score >= 60) {
            status = "양호";
            feedback = "설명문이 적절하지만 더 보완할 수 있습니다.";
        } else {
            status = "개선 필요";
            feedback = "설명문을 더 상세하게 작성하세요.";
            if (analysis != null) {
                int length = analysis.getDescriptionLength() != null ? analysis.getDescriptionLength() : 0;
                feedback += " 현재 " + length + "자, 최소 250자 권장.";
            }
        }
        
        return QualityScore.ScoreDetail.builder()
            .score(score)
            .status(status)
            .feedback(feedback)
            .build();
    }
    
    /**
     * 참여도 점수 계산
     */
    private int calculateEngagementScore(YouTubeVideo video) {
        long views = video.getViewCount() != null ? video.getViewCount() : 0;
        long likes = video.getLikeCount() != null ? video.getLikeCount() : 0;
        long comments = video.getCommentCount() != null ? video.getCommentCount() : 0;
        
        if (views == 0) return 0;
        
        double engagementRate = ((double) (likes + comments) / views) * 100;
        
        // 참여율을 점수로 변환
        int score;
        if (engagementRate >= 5.0) {
            score = 100;
        } else if (engagementRate >= 3.0) {
            score = 80;
        } else if (engagementRate >= 2.0) {
            score = 60;
        } else if (engagementRate >= 1.0) {
            score = 40;
        } else if (engagementRate >= 0.5) {
            score = 20;
        } else {
            score = 10;
        }
        
        return score;
    }
    
    /**
     * 참여도 점수 평가
     */
    private QualityScore.ScoreDetail evaluateEngagementScore(int score, YouTubeVideo video) {
        long views = video.getViewCount() != null ? video.getViewCount() : 0;
        long likes = video.getLikeCount() != null ? video.getLikeCount() : 0;
        long comments = video.getCommentCount() != null ? video.getCommentCount() : 0;
        
        double engagementRate = views > 0 ? ((double) (likes + comments) / views) * 100 : 0;
        
        String status;
        String feedback;
        
        if (score >= 80) {
            status = "우수";
            feedback = String.format("참여율이 매우 높습니다 (%.2f%%). 시청자들이 적극적으로 반응하고 있습니다.", engagementRate);
        } else if (score >= 60) {
            status = "양호";
            feedback = String.format("참여율이 적절합니다 (%.2f%%). 좋아요와 댓글을 더 유도하세요.", engagementRate);
        } else {
            status = "개선 필요";
            feedback = String.format("참여율이 낮습니다 (%.2f%%). CTA를 추가하고 시청자와 소통하세요.", engagementRate);
        }
        
        return QualityScore.ScoreDetail.builder()
            .score(score)
            .status(status)
            .feedback(feedback)
            .build();
    }
    
    /**
     * 등급 계산
     */
    private String calculateGrade(int score) {
        if (score >= 90) return "S";
        if (score >= 80) return "A";
        if (score >= 70) return "B";
        if (score >= 60) return "C";
        if (score >= 50) return "D";
        return "F";
    }
}
