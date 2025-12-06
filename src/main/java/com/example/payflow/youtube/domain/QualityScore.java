package com.example.payflow.youtube.domain;

import lombok.Builder;
import lombok.Getter;

/**
 * 영상 품질 세부 점수
 */
@Getter
@Builder
public class QualityScore {
    // 개별 점수 (각 0-100)
    private Integer titleOptimizationScore;
    private Integer tagDiversityScore;
    private Integer descriptionLengthScore;
    private Integer engagementScore;
    
    // 종합 점수
    private Integer overallScore;  // 0-100
    private String grade;  // S, A, B, C, D, F
    
    // 각 항목별 상세 설명
    private ScoreDetail titleDetail;
    private ScoreDetail tagDetail;
    private ScoreDetail descriptionDetail;
    private ScoreDetail engagementDetail;
    
    @Getter
    @Builder
    public static class ScoreDetail {
        private Integer score;
        private String status;  // "우수", "양호", "개선 필요"
        private String feedback;
    }
}
