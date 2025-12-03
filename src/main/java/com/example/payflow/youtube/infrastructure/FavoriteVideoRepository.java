package com.example.payflow.youtube.infrastructure;

import com.example.payflow.youtube.domain.FavoriteVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteVideoRepository extends JpaRepository<FavoriteVideo, Long> {
    
    List<FavoriteVideo> findByFolderIdOrderByAddedAtDesc(Long folderId);
    
    Optional<FavoriteVideo> findByFolderIdAndVideoId(Long folderId, String videoId);
    
    boolean existsByFolderIdAndVideoId(Long folderId, String videoId);
    
    void deleteByFolderIdAndVideoId(Long folderId, String videoId);
}
