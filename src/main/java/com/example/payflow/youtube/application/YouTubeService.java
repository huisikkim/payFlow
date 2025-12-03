package com.example.payflow.youtube.application;

import com.example.payflow.youtube.domain.YouTubeVideo;
import com.example.payflow.youtube.domain.YouTubeVideoStatistics;
import com.example.payflow.youtube.infrastructure.YouTubeApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class YouTubeService {

    private final YouTubeApiClient youTubeApiClient;

    /**
     * 한국 인기 급상승 영상 목록 조회 (기본 25개)
     */
    public List<YouTubeVideo> getKoreanPopularVideos() {
        return getPopularVideos("KR", 25);
    }

    /**
     * 특정 국가의 인기 급상승 영상 목록 조회 (채널 구독자 정보 포함)
     */
    public List<YouTubeVideo> getPopularVideos(String regionCode, int maxResults) {
        log.info("Getting popular videos for region: {}, maxResults: {}", regionCode, maxResults);
        List<YouTubeVideo> videos = youTubeApiClient.getMostPopularVideos(regionCode, maxResults);
        return enrichWithChannelSubscribers(videos);
    }

    /**
     * 특정 영상의 실시간 통계 조회
     */
    public YouTubeVideoStatistics getVideoStatistics(String videoId) {
        log.info("Getting statistics for video: {}", videoId);
        return youTubeApiClient.getVideoStatistics(videoId);
    }

    /**
     * 여러 영상의 통계 일괄 조회
     */
    public List<YouTubeVideoStatistics> getMultipleVideoStatistics(List<String> videoIds) {
        log.info("Getting statistics for {} videos", videoIds.size());
        return youTubeApiClient.getMultipleVideoStatistics(videoIds);
    }

    /**
     * 인기 영상 목록에서 조회수 기준 상위 N개 조회
     */
    public List<YouTubeVideo> getTopViewedVideos(String regionCode, int topN) {
        List<YouTubeVideo> videos = youTubeApiClient.getMostPopularVideos(regionCode, 50);
        return videos.stream()
                .sorted((v1, v2) -> Long.compare(
                    v2.getViewCount() != null ? v2.getViewCount() : 0,
                    v1.getViewCount() != null ? v1.getViewCount() : 0))
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * 인기 영상 목록에서 좋아요 기준 상위 N개 조회
     */
    public List<YouTubeVideo> getTopLikedVideos(String regionCode, int topN) {
        List<YouTubeVideo> videos = youTubeApiClient.getMostPopularVideos(regionCode, 50);
        return videos.stream()
                .sorted((v1, v2) -> Long.compare(
                    v2.getLikeCount() != null ? v2.getLikeCount() : 0,
                    v1.getLikeCount() != null ? v1.getLikeCount() : 0))
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * 키워드로 영상 검색 (채널 구독자 정보 포함)
     */
    public List<YouTubeVideo> searchVideos(String query, int maxResults) {
        log.info("Searching videos with query: {}, maxResults: {}", query, maxResults);
        List<YouTubeVideo> videos = youTubeApiClient.searchVideos(query, maxResults);
        return enrichWithChannelSubscribers(videos);
    }

    /**
     * 영상 목록에 채널 정보 추가 (구독자 수 + 연락처)
     */
    private List<YouTubeVideo> enrichWithChannelSubscribers(List<YouTubeVideo> videos) {
        if (videos == null || videos.isEmpty()) {
            return videos;
        }
        
        // 채널 ID 목록 추출
        List<String> channelIds = videos.stream()
                .map(YouTubeVideo::getChannelId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        // 채널 정보 조회 (구독자 수 + 설명 + 연락처)
        Map<String, YouTubeApiClient.ChannelInfo> channelInfos = youTubeApiClient.getChannelInfos(channelIds);
        
        // 영상에 채널 정보 설정
        videos.forEach(video -> {
            if (video.getChannelId() != null) {
                YouTubeApiClient.ChannelInfo info = channelInfos.get(video.getChannelId());
                if (info != null) {
                    video.setChannelSubscriberCount(info.subscriberCount);
                    video.setChannelDescription(info.description);
                    video.setChannelEmail(info.email);
                    video.setChannelInstagram(info.instagram);
                    video.setChannelTwitter(info.twitter);
                    video.setChannelWebsite(info.website);
                }
            }
        });
        
        return videos;
    }
}
