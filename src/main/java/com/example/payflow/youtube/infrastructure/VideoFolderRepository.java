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
    
    @Query("SELECT f FROM VideoFolder f LEFT JOIN FETCH f.videos WHERE f.id = :id")
    Optional<VideoFolder> findByIdWithVideos(Long id);
    
    boolean existsByName(String name);
}
