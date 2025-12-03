package com.example.payflow.youtube.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorite_videos", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"folder_id", "video_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteVideo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false)
    private VideoFolder folder;
    
    @Column(name = "video_id", nullable = false, length = 50)
    private String videoId;
    
    @Column(nullable = false, length = 500)
    private String title;
    
    @Column(name = "channel_title", length = 200)
    private String channelTitle;
    
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;
    
    @Column(name = "view_count")
    private Long viewCount;
    
    @Column(name = "like_count")
    private Long likeCount;
    
    @Column(length = 50)
    private String duration;
    
    @Column(name = "added_at")
    private LocalDateTime addedAt;
    
    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
}
