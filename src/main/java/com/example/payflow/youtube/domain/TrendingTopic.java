package com.example.payflow.youtube.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendingTopic {
    private String title;              // 트렌드 제목
    private String description;        // 설명
    private String newsUrl;           // 관련 뉴스 URL
    private String imageUrl;          // 이미지 URL
    private String traffic;           // 트래픽 정보 (예: "200K+ searches")
    private LocalDateTime publishedAt; // 발행 시간
    private int rank;                 // 순위
}
