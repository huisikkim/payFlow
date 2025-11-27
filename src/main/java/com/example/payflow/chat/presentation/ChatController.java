package com.example.payflow.chat.presentation;

import com.example.payflow.chat.application.ChatService;
import com.example.payflow.chat.domain.ChatMessage;
import com.example.payflow.chat.domain.ChatRoom;
import com.example.payflow.chat.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    
    /**
     * 채팅방 생성 또는 조회
     */
    @PostMapping("/rooms")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'DISTRIBUTOR')")
    public ResponseEntity<ChatRoomResponse> createOrGetChatRoom(
            @RequestBody CreateChatRoomRequest request,
            Authentication authentication) {
        
        ChatRoom chatRoom = chatService.getOrCreateChatRoom(
                request.getStoreId(), 
                request.getDistributorId()
        );
        
        String userId = authentication.getName();
        ChatMessage.SenderType userType = getUserType(authentication);
        long unreadCount = chatService.getUnreadCount(chatRoom.getRoomId(), userId);
        
        return ResponseEntity.ok(ChatRoomResponse.from(chatRoom, unreadCount));
    }
    
    /**
     * 내 채팅방 목록 조회
     */
    @GetMapping("/rooms")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'DISTRIBUTOR')")
    public ResponseEntity<List<ChatRoomResponse>> getMyChatRooms(Authentication authentication) {
        String userId = authentication.getName();
        ChatMessage.SenderType userType = getUserType(authentication);
        
        List<ChatRoom> chatRooms = chatService.getMyChatRooms(userId, userType);
        
        List<ChatRoomResponse> responses = chatRooms.stream()
                .map(room -> {
                    long unreadCount = chatService.getUnreadCount(room.getRoomId(), userId);
                    return ChatRoomResponse.from(room, unreadCount);
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * 채팅방 메시지 목록 조회
     */
    @GetMapping("/rooms/{roomId}/messages")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'DISTRIBUTOR')")
    public ResponseEntity<Page<ChatMessageResponse>> getMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication) {
        
        String userId = authentication.getName();
        ChatMessage.SenderType userType = getUserType(authentication);
        
        Page<ChatMessage> messages = chatService.getMessages(roomId, userId, userType, page, size);
        Page<ChatMessageResponse> responses = messages.map(ChatMessageResponse::from);
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * 메시지 읽음 처리
     */
    @PutMapping("/rooms/{roomId}/read")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'DISTRIBUTOR')")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable String roomId,
            Authentication authentication) {
        
        String userId = authentication.getName();
        chatService.markMessagesAsRead(roomId, userId);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * 읽지 않은 메시지 수 조회
     */
    @GetMapping("/rooms/{roomId}/unread-count")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'DISTRIBUTOR')")
    public ResponseEntity<Long> getUnreadCount(
            @PathVariable String roomId,
            Authentication authentication) {
        
        String userId = authentication.getName();
        long count = chatService.getUnreadCount(roomId, userId);
        
        return ResponseEntity.ok(count);
    }
    
    /**
     * 사용자 타입 추출
     */
    private ChatMessage.SenderType getUserType(Authentication authentication) {
        boolean isStore = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_STORE_OWNER"));
        
        return isStore ? ChatMessage.SenderType.STORE : ChatMessage.SenderType.DISTRIBUTOR;
    }
}
