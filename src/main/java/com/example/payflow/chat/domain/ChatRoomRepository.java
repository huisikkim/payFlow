package com.example.payflow.chat.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    Optional<ChatRoom> findByRoomId(String roomId);
    
    List<ChatRoom> findByStoreIdAndIsActiveTrue(String storeId);
    
    List<ChatRoom> findByDistributorIdAndIsActiveTrue(String distributorId);
    
    Optional<ChatRoom> findByStoreIdAndDistributorId(String storeId, String distributorId);
    
    boolean existsByRoomId(String roomId);
}
