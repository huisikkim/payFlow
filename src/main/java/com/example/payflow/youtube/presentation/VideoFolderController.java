package com.example.payflow.youtube.presentation;

import com.example.payflow.youtube.application.VideoFolderService;
import com.example.payflow.youtube.domain.FavoriteVideo;
import com.example.payflow.youtube.domain.VideoFolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/youtube/folders")
@RequiredArgsConstructor
public class VideoFolderController {
    
    private final VideoFolderService folderService;
    
    // 폴더 목록 조회 (사용자별)
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFolders() {
        String username = getCurrentUsername();
        List<VideoFolder> folders = folderService.getUserFolders(username);
        
        List<Map<String, Object>> folderList = folders.stream()
                .map(this::toFolderDto)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("folders", folderList);
        
        return ResponseEntity.ok(response);
    }
    
    // 폴더 상세 조회 (영상 포함)
    @GetMapping("/{folderId}")
    public ResponseEntity<Map<String, Object>> getFolder(@PathVariable Long folderId) {
        try {
            VideoFolder folder = folderService.getFolderWithVideos(folderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("folder", toFolderDetailDto(folder));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    // 폴더 생성
    @PostMapping
    public ResponseEntity<Map<String, Object>> createFolder(@RequestBody Map<String, String> request) {
        try {
            String username = getCurrentUsername();
            String name = request.get("name");
            String description = request.get("description");
            
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "폴더명을 입력해주세요"));
            }
            
            VideoFolder folder = folderService.createFolder(username, name.trim(), description);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("folder", toFolderDto(folder));
            response.put("message", "폴더가 생성되었습니다");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    // 폴더 수정
    @PutMapping("/{folderId}")
    public ResponseEntity<Map<String, Object>> updateFolder(
            @PathVariable Long folderId,
            @RequestBody Map<String, String> request) {
        try {
            String username = getCurrentUsername();
            String name = request.get("name");
            String description = request.get("description");
            
            VideoFolder folder = folderService.updateFolder(folderId, username, name, description);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("folder", toFolderDto(folder));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    // 폴더 삭제
    @DeleteMapping("/{folderId}")
    public ResponseEntity<Map<String, Object>> deleteFolder(@PathVariable Long folderId) {
        try {
            String username = getCurrentUsername();
            folderService.deleteFolder(folderId, username);
            return ResponseEntity.ok(Map.of("success", true, "message", "폴더가 삭제되었습니다"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    // 영상 즐겨찾기 추가
    @PostMapping("/{folderId}/videos")
    public ResponseEntity<Map<String, Object>> addVideoToFolder(
            @PathVariable Long folderId,
            @RequestBody Map<String, Object> request) {
        try {
            String username = getCurrentUsername();
            
            FavoriteVideo video = FavoriteVideo.builder()
                    .videoId((String) request.get("videoId"))
                    .title((String) request.get("title"))
                    .channelTitle((String) request.get("channelTitle"))
                    .thumbnailUrl((String) request.get("thumbnailUrl"))
                    .viewCount(toLong(request.get("viewCount")))
                    .likeCount(toLong(request.get("likeCount")))
                    .duration((String) request.get("duration"))
                    .build();
            
            FavoriteVideo saved = folderService.addVideoToFolder(folderId, username, video);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("video", toVideoDto(saved));
            response.put("message", "영상이 추가되었습니다");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    // 영상 즐겨찾기 삭제
    @DeleteMapping("/{folderId}/videos/{videoId}")
    public ResponseEntity<Map<String, Object>> removeVideoFromFolder(
            @PathVariable Long folderId,
            @PathVariable String videoId) {
        try {
            String username = getCurrentUsername();
            folderService.removeVideoFromFolder(folderId, username, videoId);
            return ResponseEntity.ok(Map.of("success", true, "message", "영상이 삭제되었습니다"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    // 폴더 내 영상 목록 조회
    @GetMapping("/{folderId}/videos")
    public ResponseEntity<Map<String, Object>> getVideosInFolder(@PathVariable Long folderId) {
        try {
            String username = getCurrentUsername();
            List<FavoriteVideo> videos = folderService.getVideosInFolder(folderId, username);
            
            List<Map<String, Object>> videoList = videos.stream()
                    .map(this::toVideoDto)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("videos", videoList);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    // 현재 로그인한 사용자명 가져오기
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }
        return authentication.getName();
    }
    
    private Map<String, Object> toFolderDto(VideoFolder folder) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", folder.getId());
        dto.put("name", folder.getName());
        dto.put("description", folder.getDescription());
        dto.put("videoCount", folder.getVideos() != null ? folder.getVideos().size() : 0);
        dto.put("createdAt", folder.getCreatedAt());
        dto.put("updatedAt", folder.getUpdatedAt());
        return dto;
    }
    
    private Map<String, Object> toFolderDetailDto(VideoFolder folder) {
        Map<String, Object> dto = toFolderDto(folder);
        dto.put("videos", folder.getVideos().stream()
                .map(this::toVideoDto)
                .collect(Collectors.toList()));
        return dto;
    }
    
    private Map<String, Object> toVideoDto(FavoriteVideo video) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", video.getId());
        dto.put("videoId", video.getVideoId());
        dto.put("title", video.getTitle());
        dto.put("channelTitle", video.getChannelTitle());
        dto.put("thumbnailUrl", video.getThumbnailUrl());
        dto.put("viewCount", video.getViewCount());
        dto.put("likeCount", video.getLikeCount());
        dto.put("duration", video.getDuration());
        dto.put("addedAt", video.getAddedAt());
        return dto;
    }
    
    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String) return Long.parseLong((String) value);
        return null;
    }
}
