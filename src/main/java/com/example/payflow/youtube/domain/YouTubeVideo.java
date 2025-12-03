package com.example.payflow.youtube.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YouTubeVideo {
    private String videoId;
    private String title;
    private String description;
    private String channelId;
    private String channelTitle;
    private String thumbnailUrl;
    private String publishedAt;
    
    // Statistics
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    
    // Additional info
    private String duration;
    private String categoryId;
    
    // Channel Statistics (for viral index calculation)
    private Long channelSubscriberCount;
    
    // Channel Contact Info (parsed from channel description)
    private String channelDescription;
    private String channelEmail;
    private String channelInstagram;
    private String channelTwitter;
    private String channelWebsite;
}
