package com.example.payflow.chat.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String roomId;  // store_{storeId}_dist_{distributorId}
    
    @Column(nullable = false)
    private String storeId;
    
    @Column(nullable = false)
    private String distributorId;
    
    private String storeName;
    private String distributorName;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastMessageAt;
    
    public ChatRoom(String storeId, String distributorId, String storeName, String distributorName) {
        this.roomId = generateRoomId(storeId, distributorId);
        this.storeId = storeId;
        this.distributorId = distributorId;
        this.storeName = storeName;
        this.distributorName = distributorName;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public static String generateRoomId(String storeId, String distributorId) {
        return "store_" + storeId + "_dist_" + distributorId;
    }
    
    public void updateLastMessageTime() {
        this.lastMessageAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
}
