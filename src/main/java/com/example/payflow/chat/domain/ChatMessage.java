package com.example.payflow.chat.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_room_id", columnList = "roomId"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String roomId;
    
    @Column(nullable = false)
    private String senderId;  // storeId or distributorId
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SenderType senderType;  // STORE or DISTRIBUTOR
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageType messageType;
    
    @Column(nullable = false, length = 2000)
    private String content;
    
    private String metadata;  // JSON 형태로 추가 정보 저장 (주문ID, 견적ID 등)
    
    @Column(nullable = false)
    private Boolean isRead = false;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    public ChatMessage(String roomId, String senderId, SenderType senderType, 
                      MessageType messageType, String content, String metadata) {
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderType = senderType;
        this.messageType = messageType;
        this.content = content;
        this.metadata = metadata;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }
    
    public void markAsRead() {
        this.isRead = true;
    }
    
    public enum SenderType {
        STORE,
        DISTRIBUTOR
    }
    
    public enum MessageType {
        TEXT,              // 일반 텍스트
        ORDER_INQUIRY,     // 주문 문의
        QUOTE_REQUEST,     // 견적 요청
        QUOTE_RESPONSE,    // 견적 응답
        SYSTEM             // 시스템 메시지
    }
}
