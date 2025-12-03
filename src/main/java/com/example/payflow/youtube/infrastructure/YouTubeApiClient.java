package com.example.payflow.youtube.infrastructure;

import com.example.payflow.youtube.domain.YouTubeVideo;
import com.example.payflow.youtube.domain.YouTubeVideoStatistics;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class YouTubeApiClient {

    private final WebClient webClient;
    
    @Value("${youtube.api.key}")
    private String apiKey;
    
    @Value("${youtube.api.base-url:https://www.googleapis.com/youtube/v3}")
    private String baseUrl;

    /**
     * 인기 급상승 영상 목록 조회
     */
    public List<YouTubeVideo> getMostPopularVideos(String regionCode, int maxResults) {
        String url = String.format(
            "%s/videos?part=snippet,statistics,contentDetails&chart=mostPopular&regionCode=%s&maxResults=%d&key=%s",
            baseUrl, regionCode, maxResults, apiKey
        );
        
        log.info("Fetching most popular videos for region: {}", regionCode);
        
        try {
            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            return parseVideosResponse(response);
        } catch (Exception e) {
            log.error("YouTube API 호출 실패: {}", e.getMessage());
            throw new RuntimeException("YouTube API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 특정 영상의 통계 정보 조회
     */
    public YouTubeVideoStatistics getVideoStatistics(String videoId) {
        String url = String.format(
            "%s/videos?part=statistics&id=%s&key=%s",
            baseUrl, videoId, apiKey
        );
        
        log.info("Fetching statistics for video: {}", videoId);
        
        String response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        
        return parseStatisticsResponse(response, videoId);
    }

    /**
     * 여러 영상의 통계 정보 일괄 조회
     */
    public List<YouTubeVideoStatistics> getMultipleVideoStatistics(List<String> videoIds) {
        String ids = String.join(",", videoIds);
        String url = String.format(
            "%s/videos?part=statistics&id=%s&key=%s",
            baseUrl, ids, apiKey
        );
        
        log.info("Fetching statistics for {} videos", videoIds.size());
        
        String response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        
        return parseMultipleStatisticsResponse(response);
    }

    private List<YouTubeVideo> parseVideosResponse(String response) {
        List<YouTubeVideo> videos = new ArrayList<>();
        
        try {
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            JsonArray items = json.getAsJsonArray("items");
            
            for (JsonElement item : items) {
                JsonObject videoObj = item.getAsJsonObject();
                JsonObject snippet = videoObj.getAsJsonObject("snippet");
                JsonObject statistics = videoObj.has("statistics") 
                    ? videoObj.getAsJsonObject("statistics") : new JsonObject();
                JsonObject contentDetails = videoObj.has("contentDetails")
                    ? videoObj.getAsJsonObject("contentDetails") : new JsonObject();
                
                YouTubeVideo video = YouTubeVideo.builder()
                    .videoId(videoObj.get("id").getAsString())
                    .title(getStringOrNull(snippet, "title"))
                    .description(getStringOrNull(snippet, "description"))
                    .channelId(getStringOrNull(snippet, "channelId"))
                    .channelTitle(getStringOrNull(snippet, "channelTitle"))
                    .thumbnailUrl(getThumbnailUrl(snippet))
                    .publishedAt(getStringOrNull(snippet, "publishedAt"))
                    .viewCount(getLongOrNull(statistics, "viewCount"))
                    .likeCount(getLongOrNull(statistics, "likeCount"))
                    .commentCount(getLongOrNull(statistics, "commentCount"))
                    .duration(getStringOrNull(contentDetails, "duration"))
                    .categoryId(getStringOrNull(snippet, "categoryId"))
                    .build();
                
                videos.add(video);
            }
        } catch (Exception e) {
            log.error("Error parsing YouTube API response", e);
        }
        
        return videos;
    }

    private YouTubeVideoStatistics parseStatisticsResponse(String response, String videoId) {
        try {
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            JsonArray items = json.getAsJsonArray("items");
            
            if (items != null && items.size() > 0) {
                JsonObject statistics = items.get(0).getAsJsonObject()
                    .getAsJsonObject("statistics");
                
                return YouTubeVideoStatistics.builder()
                    .videoId(videoId)
                    .viewCount(getLongOrNull(statistics, "viewCount"))
                    .likeCount(getLongOrNull(statistics, "likeCount"))
                    .commentCount(getLongOrNull(statistics, "commentCount"))
                    .favoriteCount(getLongOrNull(statistics, "favoriteCount"))
                    .build();
            }
        } catch (Exception e) {
            log.error("Error parsing statistics response for video: {}", videoId, e);
        }
        
        return null;
    }

    private List<YouTubeVideoStatistics> parseMultipleStatisticsResponse(String response) {
        List<YouTubeVideoStatistics> statsList = new ArrayList<>();
        
        try {
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            JsonArray items = json.getAsJsonArray("items");
            
            for (JsonElement item : items) {
                JsonObject videoObj = item.getAsJsonObject();
                JsonObject statistics = videoObj.getAsJsonObject("statistics");
                
                YouTubeVideoStatistics stats = YouTubeVideoStatistics.builder()
                    .videoId(videoObj.get("id").getAsString())
                    .viewCount(getLongOrNull(statistics, "viewCount"))
                    .likeCount(getLongOrNull(statistics, "likeCount"))
                    .commentCount(getLongOrNull(statistics, "commentCount"))
                    .favoriteCount(getLongOrNull(statistics, "favoriteCount"))
                    .build();
                
                statsList.add(stats);
            }
        } catch (Exception e) {
            log.error("Error parsing multiple statistics response", e);
        }
        
        return statsList;
    }

    private String getStringOrNull(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() 
            ? obj.get(key).getAsString() : null;
    }

    private Long getLongOrNull(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() 
            ? obj.get(key).getAsLong() : null;
    }

    private String getThumbnailUrl(JsonObject snippet) {
        if (snippet.has("thumbnails")) {
            JsonObject thumbnails = snippet.getAsJsonObject("thumbnails");
            if (thumbnails.has("high")) {
                return thumbnails.getAsJsonObject("high").get("url").getAsString();
            } else if (thumbnails.has("medium")) {
                return thumbnails.getAsJsonObject("medium").get("url").getAsString();
            } else if (thumbnails.has("default")) {
                return thumbnails.getAsJsonObject("default").get("url").getAsString();
            }
        }
        return null;
    }
}
