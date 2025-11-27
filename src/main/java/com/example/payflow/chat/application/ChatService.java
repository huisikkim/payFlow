package com.example.payflow.chat.application;

import com.example.payflow.chat.domain.*;
import com.example.payflow.distributor.domain.Distributor;
import com.example.payflow.distributor.domain.DistributorRepository;
import com.example.payflow.store.domain.Store;
import com.example.payflow.store.domain.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChatService {
    
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final StoreRepository storeRepository;
    private final DistributorRepository distributorRepository;
    
    /**
     * 채팅방 생성 또는 조회
     */
    @Transactional
    public ChatRoom getOrCreateChatRoom(String storeId, String distributorId) {
        return chatRoomRepository.findByStoreIdAndDistributorId(storeId, distributorId)
                .orElseGet(() -> {
                    Store store = storeRepository.findByStoreId(storeId)
                            .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다: " + storeId));
                    
                    Distributor distributor = distributorRepository.findByDistributorId(distributorId)
                            .orElseThrow(() -> new IllegalArgumentException("유통업체를 찾을 수 없습니다: " + distributorId));
                    
                    ChatRoom chatRoom = new ChatRoom(
                            storeId, 
                            distributorId, 
                            store.getStoreName(), 
                            distributor.getDistributorName()
                    );
                    
                    return chatRoomRepository.save(chatRoom);
                });
    }
    
    /**
     * 메시지 전송
     */
    @Transactional
    public ChatMessage sendMessage(String roomId, String senderId, ChatMessage.SenderType senderType,
                                   ChatMessage.MessageType messageType, String content, String metadata) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));
        
        // 권한 검증
        validateAccess(chatRoom, senderId, senderType);
        
        ChatMessage message = new ChatMessage(roomId, senderId, senderType, messageType, content, metadata);
        ChatMessage savedMessage = chatMessageRepository.save(message);
        
        // 채팅방 마지막 메시지 시간 업데이트
        chatRoom.updateLastMessageTime();
        
        log.info("메시지 전송 완료 - roomId: {}, senderId: {}, type: {}", roomId, senderId, messageType);
        
        return savedMessage;
    }
    
    /**
     * 채팅방 메시지 목록 조회 (페이징)
     */
    public Page<ChatMessage> getMessages(String roomId, String userId, ChatMessage.SenderType userType, int page, int size) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));
        
        validateAccess(chatRoom, userId, userType);
        
        Pageable pageable = PageRequest.of(page, size);
        return chatMessageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pageable);
    }
    
    /**
     * 내 채팅방 목록 조회
     */
    public List<ChatRoom> getMyChatRooms(String userId, ChatMessage.SenderType userType) {
        if (userType == ChatMessage.SenderType.STORE) {
            return chatRoomRepository.findByStoreIdAndIsActiveTrue(userId);
        } else {
            return chatRoomRepository.findByDistributorIdAndIsActiveTrue(userId);
        }
    }
    
    /**
     * 읽지 않은 메시지 수 조회
     */
    public long getUnreadCount(String roomId, String userId) {
        return chatMessageRepository.countUnreadMessages(roomId, userId);
    }
    
    /**
     * 메시지 읽음 처리
     */
    @Transactional
    public void markMessagesAsRead(String roomId, String userId) {
        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadMessages(roomId, userId);
        unreadMessages.forEach(ChatMessage::markAsRead);
        
        log.info("메시지 읽음 처리 완료 - roomId: {}, userId: {}, count: {}", roomId, userId, unreadMessages.size());
    }
    
    /**
     * 채팅방 접근 권한 검증
     */
    private void validateAccess(ChatRoom chatRoom, String userId, ChatMessage.SenderType userType) {
        boolean hasAccess = (userType == ChatMessage.SenderType.STORE && chatRoom.getStoreId().equals(userId)) ||
                           (userType == ChatMessage.SenderType.DISTRIBUTOR && chatRoom.getDistributorId().equals(userId));
        
        if (!hasAccess) {
            throw new IllegalArgumentException("채팅방에 접근할 권한이 없습니다.");
        }
    }
    
    /**
     * 채팅방 조회
     */
    public ChatRoom getChatRoom(String roomId) {
        return chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));
    }
}
