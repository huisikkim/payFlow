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
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
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
     * 키워드로 영상 검색
     */
    public List<YouTubeVideo> searchVideos(String query, int maxResults) {
        log.info("Searching videos with query: {}", query);
        
        // UriComponentsBuilder를 사용하여 한글 등 특수문자 인코딩 처리
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search")
                .queryParam("part", "snippet")
                .queryParam("type", "video")
                .queryParam("q", query)  // 인코딩은 UriComponentsBuilder가 자동 처리
                .queryParam("maxResults", maxResults)
                .queryParam("key", apiKey)
                .build()
                .encode()  // UTF-8 인코딩 적용
                .toUri();
        
        log.debug("Search URI: {}", uri);
        
        try {
            String response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            List<String> videoIds = parseSearchResponse(response);
            if (videoIds.isEmpty()) {
                log.info("No videos found for query: {}", query);
                return new ArrayList<>();
            }
            
            log.info("Found {} videos for query: {}", videoIds.size(), query);
            // 검색 결과의 상세 정보 조회
            return getVideoDetails(videoIds);
        } catch (Exception e) {
            log.error("YouTube 검색 API 호출 실패: {}", e.getMessage());
            throw new RuntimeException("YouTube 검색 API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 영상 ID 목록으로 상세 정보 조회
     */
    public List<YouTubeVideo> getVideoDetails(List<String> videoIds) {
        String ids = String.join(",", videoIds);
        
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/videos")
                .queryParam("part", "snippet,statistics,contentDetails")
                .queryParam("id", ids)
                .queryParam("key", apiKey)
                .build()
                .encode()
                .toUri();
        
        String response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        
        return parseVideosResponse(response);
    }

    /**
     * 인기 급상승 영상 목록 조회
     */
    public List<YouTubeVideo> getMostPopularVideos(String regionCode, int maxResults) {
        log.info("Fetching most popular videos for region: {}", regionCode);
        
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/videos")
                .queryParam("part", "snippet,statistics,contentDetails")
                .queryParam("chart", "mostPopular")
                .queryParam("regionCode", regionCode)
                .queryParam("maxResults", maxResults)
                .queryParam("key", apiKey)
                .build()
                .encode()
                .toUri();
        
        try {
            String response = webClient.get()
                    .uri(uri)
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
        log.info("Fetching statistics for video: {}", videoId);
        
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/videos")
                .queryParam("part", "statistics")
                .queryParam("id", videoId)
                .queryParam("key", apiKey)
                .build()
                .encode()
                .toUri();
        
        String response = webClient.get()
                .uri(uri)
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
        log.info("Fetching statistics for {} videos", videoIds.size());
        
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/videos")
                .queryParam("part", "statistics")
                .queryParam("id", ids)
                .queryParam("key", apiKey)
                .build()
                .encode()
                .toUri();
        
        String response = webClient.get()
                .uri(uri)
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

    private List<String> parseSearchResponse(String response) {
        List<String> videoIds = new ArrayList<>();
        
        try {
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            JsonArray items = json.getAsJsonArray("items");
            
            for (JsonElement item : items) {
                JsonObject idObj = item.getAsJsonObject().getAsJsonObject("id");
                if (idObj.has("videoId")) {
                    videoIds.add(idObj.get("videoId").getAsString());
                }
            }
        } catch (Exception e) {
            log.error("Error parsing search response", e);
        }
        
        return videoIds;
    }

    /**
     * 채널 정보 조회 (구독자 수 + 설명 + 연락처 파싱)
     */
    public java.util.Map<String, ChannelInfo> getChannelInfos(List<String> channelIds) {
        java.util.Map<String, ChannelInfo> channelInfos = new java.util.HashMap<>();
        
        if (channelIds == null || channelIds.isEmpty()) {
            return channelInfos;
        }
        
        // 중복 제거
        List<String> uniqueChannelIds = channelIds.stream().distinct().collect(java.util.stream.Collectors.toList());
        String ids = String.join(",", uniqueChannelIds);
        
        log.info("Fetching channel info for {} channels", uniqueChannelIds.size());
        
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/channels")
                .queryParam("part", "statistics,snippet")
                .queryParam("id", ids)
                .queryParam("key", apiKey)
                .build()
                .encode()
                .toUri();
        
        try {
            String response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            JsonArray items = json.getAsJsonArray("items");
            
            if (items != null) {
                for (JsonElement item : items) {
                    JsonObject channelObj = item.getAsJsonObject();
                    String channelId = channelObj.get("id").getAsString();
                    JsonObject statistics = channelObj.getAsJsonObject("statistics");
                    JsonObject snippet = channelObj.getAsJsonObject("snippet");
                    
                    ChannelInfo info = new ChannelInfo();
                    
                    // 구독자 수
                    boolean isHidden = statistics.has("hiddenSubscriberCount") 
                        && statistics.get("hiddenSubscriberCount").getAsBoolean();
                    if (!isHidden && statistics.has("subscriberCount")) {
                        info.subscriberCount = statistics.get("subscriberCount").getAsLong();
                    }
                    
                    // 채널 설명
                    String description = getStringOrNull(snippet, "description");
                    info.description = description;
                    
                    // 연락처 파싱
                    if (description != null) {
                        info.email = parseEmail(description);
                        info.instagram = parseInstagram(description);
                        info.twitter = parseTwitter(description);
                        info.website = parseWebsite(description);
                    }
                    
                    channelInfos.put(channelId, info);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching channel info: {}", e.getMessage());
        }
        
        return channelInfos;
    }
    
    /**
     * 채널 구독자 수 조회 (하위 호환성 유지)
     */
    public java.util.Map<String, Long> getChannelSubscriberCounts(List<String> channelIds) {
        java.util.Map<String, ChannelInfo> infos = getChannelInfos(channelIds);
        java.util.Map<String, Long> counts = new java.util.HashMap<>();
        infos.forEach((id, info) -> {
            if (info.subscriberCount != null) {
                counts.put(id, info.subscriberCount);
            }
        });
        return counts;
    }
    
    // 이메일 파싱
    private String parseEmail(String text) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        );
        java.util.regex.Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }
    
    // 인스타그램 파싱
    private String parseInstagram(String text) {
        // instagram.com/username 또는 @username 패턴
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(?:instagram\\.com/|insta(?:gram)?[:\\s]*@?)([a-zA-Z0-9_.]+)",
            java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher matcher = pattern.matcher(text);
        return matcher.find() ? "@" + matcher.group(1) : null;
    }
    
    // 트위터/X 파싱
    private String parseTwitter(String text) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(?:twitter\\.com/|x\\.com/|twitter[:\\s]*@?)([a-zA-Z0-9_]+)",
            java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher matcher = pattern.matcher(text);
        return matcher.find() ? "@" + matcher.group(1) : null;
    }
    
    // 웹사이트 파싱
    private String parseWebsite(String text) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}[/a-zA-Z0-9._-]*)",
            java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher matcher = pattern.matcher(text);
        // 유튜브, 인스타, 트위터 링크 제외
        while (matcher.find()) {
            String url = matcher.group(1);
            if (!url.contains("youtube.com") && !url.contains("youtu.be") 
                && !url.contains("instagram.com") && !url.contains("twitter.com")
                && !url.contains("x.com") && !url.contains("facebook.com")) {
                return url;
            }
        }
        return null;
    }
    
    // 채널 정보 내부 클래스
    public static class ChannelInfo {
        public Long subscriberCount;
        public String description;
        public String email;
        public String instagram;
        public String twitter;
        public String website;
    }

}
