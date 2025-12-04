package com.example.payflow.youtube.application;

import com.example.payflow.youtube.domain.FavoriteVideo;
import com.example.payflow.youtube.domain.VideoFolder;
import com.example.payflow.youtube.infrastructure.FavoriteVideoRepository;
import com.example.payflow.youtube.infrastructure.VideoFolderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoFolderService {
    
    private final VideoFolderRepository folderRepository;
    private final FavoriteVideoRepository favoriteVideoRepository;
    
    // 폴더 목록 조회 (전체 - 관리자용)
    @Transactional(readOnly = true)
    public List<VideoFolder> getAllFolders() {
        return folderRepository.findAllByOrderByCreatedAtDesc();
    }
    
    // 사용자별 폴더 목록 조회
    @Transactional(readOnly = true)
    public List<VideoFolder> getUserFolders(String username) {
        return folderRepository.findByUsernameOrderByCreatedAtDesc(username);
    }
    
    // 폴더 상세 조회 (영상 포함)
    @Transactional(readOnly = true)
    public VideoFolder getFolderWithVideos(Long folderId) {
        return folderRepository.findByIdWithVideos(folderId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다: " + folderId));
    }
    
    // 사용자별 폴더 상세 조회 (영상 포함, 권한 체크)
    @Transactional(readOnly = true)
    public VideoFolder getUserFolderWithVideos(Long folderId, String username) {
        return folderRepository.findByIdAndUsernameWithVideos(folderId, username)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없거나 접근 권한이 없습니다."));
    }
    
    // 폴더 생성
    @Transactional
    public VideoFolder createFolder(String username, String name, String description) {
        if (folderRepository.existsByUsernameAndName(username, name)) {
            throw new IllegalArgumentException("이미 존재하는 폴더명입니다: " + name);
        }
        
        VideoFolder folder = VideoFolder.builder()
                .username(username)
                .name(name)
                .description(description)
                .build();
        
        return folderRepository.save(folder);
    }
    
    // 폴더 수정
    @Transactional
    public VideoFolder updateFolder(Long folderId, String username, String name, String description) {
        VideoFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다: " + folderId));
        
        // 권한 체크
        if (!folder.getUsername().equals(username)) {
            throw new IllegalArgumentException("폴더를 수정할 권한이 없습니다.");
        }
        
        folder.setName(name);
        folder.setDescription(description);
        
        return folderRepository.save(folder);
    }
    
    // 폴더 삭제
    @Transactional
    public void deleteFolder(Long folderId, String username) {
        VideoFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다: " + folderId));
        
        // 권한 체크
        if (!folder.getUsername().equals(username)) {
            throw new IllegalArgumentException("폴더를 삭제할 권한이 없습니다.");
        }
        
        folderRepository.deleteById(folderId);
    }
    
    // 영상 즐겨찾기 추가
    @Transactional
    public FavoriteVideo addVideoToFolder(Long folderId, String username, FavoriteVideo video) {
        VideoFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다: " + folderId));
        
        // 권한 체크
        if (!folder.getUsername().equals(username)) {
            throw new IllegalArgumentException("폴더에 영상을 추가할 권한이 없습니다.");
        }
        
        if (favoriteVideoRepository.existsByFolderIdAndVideoId(folderId, video.getVideoId())) {
            throw new IllegalArgumentException("이미 추가된 영상입니다");
        }
        
        video.setFolder(folder);
        return favoriteVideoRepository.save(video);
    }
    
    // 영상 즐겨찾기 삭제
    @Transactional
    public void removeVideoFromFolder(Long folderId, String username, String videoId) {
        VideoFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다: " + folderId));
        
        // 권한 체크
        if (!folder.getUsername().equals(username)) {
            throw new IllegalArgumentException("폴더에서 영상을 삭제할 권한이 없습니다.");
        }
        
        favoriteVideoRepository.deleteByFolderIdAndVideoId(folderId, videoId);
    }
    
    // 폴더 내 영상 목록 조회
    @Transactional(readOnly = true)
    public List<FavoriteVideo> getVideosInFolder(Long folderId, String username) {
        VideoFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다: " + folderId));
        
        // 권한 체크
        if (!folder.getUsername().equals(username)) {
            throw new IllegalArgumentException("폴더를 조회할 권한이 없습니다.");
        }
        
        return favoriteVideoRepository.findByFolderIdOrderByAddedAtDesc(folderId);
    }
    
    // 영상이 특정 폴더에 있는지 확인
    @Transactional(readOnly = true)
    public boolean isVideoInFolder(Long folderId, String videoId) {
        return favoriteVideoRepository.existsByFolderIdAndVideoId(folderId, videoId);
    }
}
