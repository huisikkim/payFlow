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
    
    // 폴더 목록 조회
    @Transactional(readOnly = true)
    public List<VideoFolder> getAllFolders() {
        return folderRepository.findAllByOrderByCreatedAtDesc();
    }
    
    // 폴더 상세 조회 (영상 포함)
    @Transactional(readOnly = true)
    public VideoFolder getFolderWithVideos(Long folderId) {
        return folderRepository.findByIdWithVideos(folderId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다: " + folderId));
    }
    
    // 폴더 생성
    @Transactional
    public VideoFolder createFolder(String name, String description) {
        if (folderRepository.existsByName(name)) {
            throw new IllegalArgumentException("이미 존재하는 폴더명입니다: " + name);
        }
        
        VideoFolder folder = VideoFolder.builder()
                .name(name)
                .description(description)
                .build();
        
        return folderRepository.save(folder);
    }
    
    // 폴더 수정
    @Transactional
    public VideoFolder updateFolder(Long folderId, String name, String description) {
        VideoFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다: " + folderId));
        
        folder.setName(name);
        folder.setDescription(description);
        
        return folderRepository.save(folder);
    }
    
    // 폴더 삭제
    @Transactional
    public void deleteFolder(Long folderId) {
        if (!folderRepository.existsById(folderId)) {
            throw new IllegalArgumentException("폴더를 찾을 수 없습니다: " + folderId);
        }
        folderRepository.deleteById(folderId);
    }
    
    // 영상 즐겨찾기 추가
    @Transactional
    public FavoriteVideo addVideoToFolder(Long folderId, FavoriteVideo video) {
        VideoFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다: " + folderId));
        
        if (favoriteVideoRepository.existsByFolderIdAndVideoId(folderId, video.getVideoId())) {
            throw new IllegalArgumentException("이미 추가된 영상입니다");
        }
        
        video.setFolder(folder);
        return favoriteVideoRepository.save(video);
    }
    
    // 영상 즐겨찾기 삭제
    @Transactional
    public void removeVideoFromFolder(Long folderId, String videoId) {
        favoriteVideoRepository.deleteByFolderIdAndVideoId(folderId, videoId);
    }
    
    // 폴더 내 영상 목록 조회
    @Transactional(readOnly = true)
    public List<FavoriteVideo> getVideosInFolder(Long folderId) {
        return favoriteVideoRepository.findByFolderIdOrderByAddedAtDesc(folderId);
    }
    
    // 영상이 특정 폴더에 있는지 확인
    @Transactional(readOnly = true)
    public boolean isVideoInFolder(Long folderId, String videoId) {
        return favoriteVideoRepository.existsByFolderIdAndVideoId(folderId, videoId);
    }
}
