package com.example.payflow.chat.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    Page<ChatMessage> findByRoomIdOrderByCreatedAtDesc(String roomId, Pageable pageable);
    
    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(String roomId);
    
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.roomId = :roomId AND m.isRead = false AND m.senderId != :userId")
    long countUnreadMessages(@Param("roomId") String roomId, @Param("userId") String userId);
    
    @Query("SELECT m FROM ChatMessage m WHERE m.roomId = :roomId AND m.isRead = false AND m.senderId != :userId")
    List<ChatMessage> findUnreadMessages(@Param("roomId") String roomId, @Param("userId") String userId);
}
