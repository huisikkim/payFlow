package com.example.payflow.youtube.infrastructure;

import com.example.payflow.youtube.domain.VideoFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoFolderRepository extends JpaRepository<VideoFolder, Long> {
    
    List<VideoFolder> findAllByOrderByCreatedAtDesc();
    
    // 사용자별 폴더 조회
    List<VideoFolder> findByUsernameOrderByCreatedAtDesc(String username);
    
    @Query("SELECT f FROM VideoFolder f LEFT JOIN FETCH f.videos WHERE f.id = :id")
    Optional<VideoFolder> findByIdWithVideos(Long id);
    
    // 사용자별 폴더 조회 (영상 포함)
    @Query("SELECT f FROM VideoFolder f LEFT JOIN FETCH f.videos WHERE f.id = :id AND f.username = :username")
    Optional<VideoFolder> findByIdAndUsernameWithVideos(Long id, String username);
    
    boolean existsByName(String name);
    
    // 사용자별 폴더명 중복 체크
    boolean existsByUsernameAndName(String username, String name);
}
