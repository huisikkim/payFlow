package com.example.payflow.youtube.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YouTubeVideoStatistics {
    private String videoId;
    private Long viewCount;
    private Long likeCount;
    private Long dislikeCount;
    private Long commentCount;
    private Long favoriteCount;
}
