package com.example.payflow.chat.presentation.dto;

import com.example.payflow.chat.domain.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatRoomResponse {
    private Long id;
    private String roomId;
    private String storeId;
    private String distributorId;
    private String storeName;
    private String distributorName;
    private Boolean isActive;
    private LocalDateTime lastMessageAt;
    private Long unreadCount;
    
    public static ChatRoomResponse from(ChatRoom chatRoom, Long unreadCount) {
        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getRoomId(),
                chatRoom.getStoreId(),
                chatRoom.getDistributorId(),
                chatRoom.getStoreName(),
                chatRoom.getDistributorName(),
                chatRoom.getIsActive(),
                chatRoom.getLastMessageAt(),
                unreadCount
        );
    }
}
